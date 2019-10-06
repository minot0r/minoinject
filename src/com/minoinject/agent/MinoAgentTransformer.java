package com.minoinject.agent;

import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.ProtectionDomain;
import java.util.Iterator;
import java.util.logging.Logger;
import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.NotFoundException;

public class MinoAgentTransformer implements ClassFileTransformer {

	private final static Logger LOGGER = MinoAgent.getLogger();
	private final static String targetClassname = "main.Main";

	@Override
	public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer)
			throws IllegalClassFormatException {
		try {
			if(!className.startsWith("main/Main")) return classfileBuffer;
			
			LOGGER.info(className);
			
			ClassPool classPool = new ClassPool(true);
			
			classPool.appendClassPath("Simple.jar");
			importJDK(classPool);
			//classPool.appendClassPath("C:\\Program Files\\Java\\jdk1.8.0_221\\jre\\lib\\rt.jar");
			Iterator<String> iter = classPool.getImportedPackages();
			while(iter.hasNext()) LOGGER.info(iter.next());
			
		    CtClass ctClass = classPool.get(targetClassname);
		    LOGGER.info("Got class " + targetClassname);
			
		    //ctClass.stopPruning(true);

		    if (ctClass.isFrozen()) {
				System.out.println("frozen");
		    	ctClass.detach();
		    	return classfileBuffer;
		        //ctClass.defrost();
		    }
		    
		    CtMethod method = ctClass.getDeclaredMethod("simpleFunc");
		    
		    LOGGER.info("Catch method \"simpleFunc()\"");
		    //method.insertBefore("{ return; }");
		    method.insertBefore("{ java.lang.System.out.println(\"Modified from JavaAgent!\"); }");
		    //method.insertBefore("{ int x = 1; }");
		    
		    LOGGER.info("Changed method \"simpleFunc()\"");
            byte[] byteCode = ctClass.toBytecode();
		    ctClass.detach();
            return byteCode;
		} catch (NotFoundException | CannotCompileException | IOException e) {
			e.printStackTrace();
		}
		
		
		return classfileBuffer;
	}
	
	private void importJDK(ClassPool classPool) {
		try {
			Path javaHome = FileSystems.getDefault().getPath("C:\\Program Files\\Java\\jdk1.8.0_221"); // I know its hardcoded but I'll figure it out later
			Files.walk(javaHome).filter(Files::isRegularFile)
					.forEach((file) -> {
						if(file.toAbsolutePath().toUri().getPath().substring(1).endsWith(".jar")) {
							try {
								classPool.appendClassPath(file.toAbsolutePath().toUri().getPath().substring(1));
							} catch (NotFoundException e) {
								e.printStackTrace();
							}
						}
					});
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}
