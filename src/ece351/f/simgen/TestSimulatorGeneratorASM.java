/* *********************************************************************
 * ECE351 
 * Department of Electrical and Computer Engineering 
 * University of Waterloo 
 * Term: Winter 2019 (1191)
 *
 * The base version of this file is the intellectual property of the
 * University of Waterloo. Redistribution is prohibited.
 *
 * By pushing changes to this file I affirm that I am the author of
 * all changes. I affirm that I have complied with the course
 * collaboration policy and have not plagiarized my work. 
 *
 * I understand that redistributing this file might expose me to
 * disciplinary action under UW Policy 71. I understand that Policy 71
 * allows for retroactive modification of my final grade in a course.
 * For example, if I post my solutions to these labs on GitHub after I
 * finish ECE351, and a future student plagiarizes them, then I too
 * could be found guilty of plagiarism. Consequently, my final grade
 * in ECE351 could be retroactively lowered. This might require that I
 * repeat ECE351, which in turn might delay my graduation.
 *
 * https://uwaterloo.ca/secretariat-general-counsel/policies-procedures-guidelines/policy-71
 * 
 * ********************************************************************/

package ece351.f.simgen;

import static org.junit.Assert.assertTrue;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.RemappingMethodAdapter;
import org.objectweb.asm.commons.SimpleRemapper;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

import ece351.f.FParser;
import ece351.f.analysis.DetermineInputVars;
import ece351.f.ast.FProgram;
import ece351.util.BaseTest351;
import ece351.util.CommandLine;
import ece351.util.Debug;
import ece351.util.ExaminableProperties;
import ece351.util.TestInputs351;
import ece351.w.ast.WProgram;
import ece351.w.parboiled.WParboiledParser;


@RunWith(Parameterized.class)
public class TestSimulatorGeneratorASM extends BaseTest351 {

	/** The test parameters. */
	protected final File input;
	protected final int reps;
	
	// computed from the test parameter in computeFileNames()
	protected String waveName;
	protected String outputWaveName;
	protected String staffWavePath;
	protected String sourcePath;

	public final static String sep = File.separator;

	/** This constructor must be public so JUnit knows to use it. */
	public TestSimulatorGeneratorASM(final File f) {
		this(f, 1);
	}
	
	/** This constructor cannot be public of JUnit will get confused. */
	protected TestSimulatorGeneratorASM(final File f, final int reps) {
		this.input = f;
		this.reps = reps;
	}

	@Parameterized.Parameters
	public static Collection<Object[]> files() {
		return TestInputs351.formulaFiles();
	}
	
	@Test
	public void simgen() throws IOException {
		final String inputSpec = input.getAbsolutePath();
		if (inputSpec.contains("jvarty")) {
			// these files have two many variables in them
			// inconvenient to generate the appropriate wave inputs
			return;
		}
		if (inputSpec.contains("opt4") || inputSpec.contains("opt5")) {
			// these optimizations were harder,
			// so some people might not have done them
			return;
		}
		
		final CommandLine c = new CommandLine("-p", inputSpec);
		final String input = c.readInputSpec();
		System.out.println("processing " + inputSpec);
		System.out.println("input: " + inputSpec);
		System.out.println(input);
		
		// parse from the F file
		final FProgram original = FParser.parse(c);
		assertTrue(original.repOk());
		computeFileNames(inputSpec, original);

		// generate the Jasmin output
		final StringWriter sw = new StringWriter();
		final SimulatorGeneratorASM sg = new SimulatorGeneratorASM();
		sg.generate(c.getInputName(), original, new PrintWriter(sw));
		sw.close();
		final String javasim = sw.toString();
		System.out.println("output:");
		System.out.println(javasim);
		// write generated Jasmin source to file
		final File f = new File(sourcePath);
		final PrintWriter pw = new PrintWriter(new FileWriter(sourcePath));
		pw.write(javasim);
		pw.close();

		// compile the generated Jasmin file
		jasmin.Main.main(new String[] {"-d", f.getParent(), sourcePath});
		
		// merge in the main method produced in last lab
		// read the bytecode produced by Javac in last lab
		final Path javacbytepath = (new File(sourcePath.replace("_asm.jasmin", ".class")).toPath());
		ClassReader cr = new ClassReader(Files.readAllBytes(javacbytepath)); 
		ClassNode cn = new ClassNode();
		cr.accept(cn, ClassReader.EXPAND_FRAMES);
		// read the bytecode produced by Jasmin and merge in javac's main method
		final Path jasminbytepath = (new File(sourcePath.replace(".jasmin", ".class")).toPath());
		final ClassWriter cw = new ClassWriter(0);
		final ClassVisitor cv = new MergeAdapter(cw, cn);
		final ClassReader cr2 = new ClassReader(Files.readAllBytes(jasminbytepath));
		cr2.accept(cv, 0);
		final OutputStream os = new BufferedOutputStream(new FileOutputStream(jasminbytepath.toFile()));
		os.write(cw.toByteArray());
		os.close();

		// remove the old output wave file
		final File owf = new File(outputWaveName);
		if (owf.exists()) {
			owf.delete();
		}
		
		// test the compiled output
		final String classPath = "file:///" + f.getParent() + sep;
		final URLClassLoader loader = new URLClassLoader(new URL[]{new URL(classPath)});
		Class<?> simulatorClass = null;
		try {
			final String className = f.getName().replace(".jasmin", "");
			simulatorClass = Class.forName(className, true, loader);
			final Method m = simulatorClass.getMethod("main", new Class[] { String[].class });
			final Object[] args = new Object[] { new String[] { waveName, "-f", outputWaveName } };
			for (int i = 0; i < reps; i++) {
				// main is a static method
				// so we should not need to instantiate the simulatorClass
//				m.invoke(simulatorClass.newInstance(), args);
				m.invoke(null, args);
			}
			
		} catch (ClassNotFoundException e) {
			Debug.barf(e.toString());
		} catch (IllegalAccessException e) {
			Debug.barf(e.toString());
		} catch (IllegalArgumentException e) {
			Debug.barf(e.toString());
		} catch (InvocationTargetException e) {
			//special case since we're getting a exception during execution of main(),
			//and "Invocation Target Exception" is rather unhelpful.
			//also, e.getCause().getMessage() might be null
			final StringWriter trace = new StringWriter();
			trace.append("Exception raised during simulation: ");
			e.getCause().printStackTrace(new PrintWriter(trace));
			Debug.barf(trace.toString());
		} catch (NoSuchMethodException e) {
			Debug.barf(e.toString());
		} catch (SecurityException e) {
			Debug.barf(e.toString());
//		} catch (InstantiationException e) {
//			Debug.barf(e.toString());
		} 
		
		// compare the computed wave outputs
		final CommandLine csw = new CommandLine(outputWaveName);
		final WProgram studentW = WParboiledParser.parse(csw.readInputSpec());

		final CommandLine staffWaveCmd = new CommandLine(staffWavePath);
		final String StaffWave = staffWaveCmd.readInputSpec();
		final WProgram staffWProgram = WParboiledParser.parse(StaffWave);

		// check if staff/student programs are isomorphic
		assertTrue("wave outputs differ for simulation of " + inputSpec,
				staffWProgram.isomorphic(studentW));

		ExaminableProperties.checkAllUnary(staffWProgram);
		ExaminableProperties.checkAllUnary(studentW);
		ExaminableProperties.checkAllBinary(staffWProgram, studentW);

		// success!
		System.out.println("Success! " + inputSpec);
	}

	protected void computeFileNames(final String inputSpec, final FProgram fp) {
		// determine the name of the wave input to use for this formula
		final Set<String> inputVars = DetermineInputVars.inputVars(fp);
		final StringBuilder waveNameBuilder = new StringBuilder("tests/wave/");
		for (final String s : inputVars) {
			waveNameBuilder.append(s);
		}
		if (inputVars.isEmpty()) {
			waveNameBuilder.append("r1");
		}
		waveNameBuilder.append(".wave");
		waveName = waveNameBuilder.toString();
		assert (new File(waveName)).exists() : "input wave file doesn't exist: " + waveName;

		outputWaveName = inputSpec
				.replace(sep + "f" + sep, sep + "f" + sep + "student.out" + sep + "simulator" + sep)
				.replace(".f", "_asm.wave");
		
		staffWavePath = inputSpec
				.replace(sep + "f" + sep, sep + "f" + sep + "staff.out" + sep + "simulator" + sep)
				.replace(".f", ".wave");
		assert ((new File(staffWavePath)).exists()) : "staff wave file does not exist: " + staffWavePath;

		sourcePath = inputSpec.replace(sep + "f" + sep, sep + "f" + sep + "student.out" + sep + "simulator" + sep + "Simulator_").replace(".f", "_asm.jasmin");
		final File f = new File(sourcePath);
		f.getParentFile().mkdirs();

	}

}

/**
 * Code adapted from AOSD'07 paper "Using the ASM framework to implement common Java 
 * bytecode transformation patterns". Updated for ASM4 and Java5.
 * 
 * @see http://asm.ow2.org/current/asm-transformations.pdf
 */

class MergeAdapter extends ClassVisitor {
	private ClassNode cn;
	private String cname;

	public MergeAdapter(ClassVisitor cv, ClassNode cn) {
		super(Opcodes.ASM4, cv);
		this.cn = cn;
	}

	public void visit(int version, int access, String name, String signature,
			String superName, String[] interfaces) {
		super.visit(version, access, name, signature, superName, interfaces);
		this.cname = name;
	}

	@SuppressWarnings("unchecked")
	public void visitEnd() {
		for (final Iterator<FieldNode> it = cn.fields.iterator(); it.hasNext();) {
			it.next().accept(this);
		}
		for (final Iterator<MethodNode> it = cn.methods.iterator(); it.hasNext();) {
			final MethodNode mn = it.next();
			if (mn.name.equals("main")) {
				final String[] exceptions = new String[mn.exceptions.size()];
				mn.exceptions.toArray(exceptions);
				final MethodVisitor mv = cv.visitMethod(mn.access, mn.name, mn.desc,
						mn.signature, exceptions);
				mn.instructions.resetLabels();
				mn.accept(new RemappingMethodAdapter(mn.access, mn.desc, mv,
						new SimpleRemapper(cn.name, cname)));
			}
		}
		super.visitEnd();
	}
}
