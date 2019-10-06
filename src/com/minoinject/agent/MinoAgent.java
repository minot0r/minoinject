package com.minoinject.agent;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class MinoAgent {
	
	private final static Logger LOGGER = Logger.getLogger("AGENT");
	private static List<String> targetClasses = new LinkedList<String>();

	/* void agentmain(...) is called when 
	 * using VirtualMachine#loadAgent(someJavaAgent)
	 */
	
	public static void agentmain(String hookerArgs, Instrumentation inst) throws InterruptedException {
		targetClasses.add("main.Main");
		
		LOGGER.info("Hooking jar");
		
		ClassFileTransformer transformer = new MinoAgentTransformer();
		LOGGER.info("Initialized MinoAgentTransformer");
		
		try {
			/* Allowing retransformation 
			 * using Instrumentation#addTransformer(ClassFileTransformer transformer, boolean canRetransform)
			 */
			inst.addTransformer(transformer, true);
			
			Stream<String> classStream = targetClasses.stream();
			Stream<?> remainClasses = classStream
					.map((klass) -> {
						try {
							return Class.forName(klass);
						} catch (ClassNotFoundException e) {
							e.printStackTrace();
						}
						return null;
					})
					.filter((klass) -> klass != null && inst.isModifiableClass(klass));

			@SuppressWarnings("unchecked")
			ArrayList<Class<?>> targetKlasses = (ArrayList<Class<?>>) remainClasses.collect(Collectors.toCollection(ArrayList::new));
			
			StringBuilder sb = new StringBuilder();
			for(Class<?> klass : targetKlasses) {	
				sb.append("° " + klass.getName() + "; Methods : \n");
				for(Method method : klass.getMethods())
					sb.append("\t=> " + method.getName() + "()\n");
			}
			
			System.out.println(sb.toString());
				
			inst.retransformClasses(targetKlasses.toArray(new Class<?>[targetClasses.size()]));
		} catch (UnmodifiableClassException e) {
			e.printStackTrace();
		}
	}
	
	public static Logger getLogger() {
		return LOGGER;
	}
	
}
