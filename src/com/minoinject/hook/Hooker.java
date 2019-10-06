package com.minoinject.hook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.logging.Logger;

import com.minoinject.agent.MinoAgent;
import com.minoinject.agent.MinoAgentTransformer;
import com.sun.tools.attach.AgentInitializationException;
import com.sun.tools.attach.AgentLoadException;
import com.sun.tools.attach.AttachNotSupportedException;
import com.sun.tools.attach.VirtualMachine;
import com.sun.tools.attach.VirtualMachineDescriptor;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.NotFoundException;

public class Hooker {
	
	private final static Logger LOGGER = Logger.getLogger(Hooker.class.getName());
	private final static String targetClassname = "Simple";

	public static void main(String[] args) throws InterruptedException, IOException {
		LOGGER.info("Agent started");
		for(VirtualMachineDescriptor vmd : VirtualMachine.list()) {
			LOGGER.info(vmd.displayName());
			if(!vmd.displayName().startsWith(targetClassname)) continue;
			LOGGER.info("Got da VM boi");
			try {
				VirtualMachine vm = VirtualMachine.attach(vmd);
				LOGGER.info("Attached da VM");
				File jar = buildAgent();
				LOGGER.info("Build agent ma boi");
				vm.loadAgent(jar.getAbsolutePath());
				LOGGER.info("Loaded agent in target VM");
				vm.detach();
				LOGGER.info("Detached ma VM cool");
			} catch (AttachNotSupportedException | IOException | AgentLoadException | AgentInitializationException e) {
				e.printStackTrace();
			}
		}
		LOGGER.info("Agent work is finished");
	}
	
	public static File buildAgent() throws IOException {
		File agentJar = File.createTempFile("minoagent", ".jar");
		agentJar.deleteOnExit(); /* Will be deleted once this program is closed */
		
		Manifest agentManifest = new Manifest();
		Attributes maniAttributes = agentManifest.getMainAttributes();
		maniAttributes.put(Attributes.Name.MANIFEST_VERSION, "1.0");
		maniAttributes.put(new Attributes.Name("Agent-Class"), MinoAgent.class.getName());
		maniAttributes.put(new Attributes.Name("Can-Retransform-Classes"), "true");
		maniAttributes.put(new Attributes.Name("Can-Redefine-Classes"), "true");
		maniAttributes.put(new Attributes.Name("Permissions"), "all-permissions");
		//maniAttributes.put(new Attributes.Name("Boot-Class-Path"), MinoAgent.class.getName());
		
		try (JarOutputStream jos = new JarOutputStream(new FileOutputStream(agentJar), agentManifest)) {
			JarEntry agent = new JarEntry(MinoAgent.class.getName().replace('.', '/') + ".class");
			JarEntry agentTransformer = new JarEntry(MinoAgentTransformer.class.getName().replace('.', '/') + ".class");
			ClassPool pool = ClassPool.getDefault();
			
			
			
			jos.putNextEntry(agent);
			CtClass agentClass = pool.get(MinoAgent.class.getName());
			CtClass agentTransformerClass = pool.get(MinoAgentTransformer.class.getName());
			jos.write(agentClass.toBytecode());
			jos.putNextEntry(agentTransformer);
			jos.write(agentTransformerClass.toBytecode());
			jos.closeEntry();
			agentClass.detach();
			agentTransformerClass.detach();
		} catch (NotFoundException | CannotCompileException e) {
			e.printStackTrace();
		}
		
		return agentJar;
	}

	
}