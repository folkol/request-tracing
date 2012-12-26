import java.util.*;
import java.io.*;
import com.sun.jdi.*;
import com.sun.jdi.connect.*;
import com.sun.jdi.event.*;
import com.sun.jdi.request.*;

public class FieldMonitor {

		public static final String CLASS_NAME = "java.lang.Thread";
		public String classFilter;

		public static void main(String[] args)
				throws IOException, InterruptedException {
				// connect
				VirtualMachine vm = new VMAcquirer().connect(8000);
				
				// set watch field on already loaded classes
				List<ReferenceType> referenceTypes = vm
						.classesByName(CLASS_NAME);
				for (ReferenceType refType : referenceTypes) {
						//      addFieldWatch(vm, refType);
						addMethodEntryRequest(vm, refType);
				}
				// watch for loaded classes
				addClassWatch(vm);
				
				
				// resume the vm
				vm.resume();
				
				// process events
				EventQueue eventQueue = vm.eventQueue();
				while (true) {
						EventSet eventSet = eventQueue.remove();
						for (Event event : eventSet) {
								if (event instanceof VMDeathEvent
										|| event instanceof VMDisconnectEvent) {
										// exit
										return;
								} else if (event instanceof ClassPrepareEvent) {
										// watch field on loaded class
										ClassPrepareEvent classPrepEvent = (ClassPrepareEvent) event;
										ReferenceType refType = classPrepEvent
												.referenceType();
										addMethodEntryRequest(vm, refType);
										//          addFieldWatch(vm, refType);
								} else if (event instanceof ModificationWatchpointEvent) {
										// a Test.foo has changed
										ModificationWatchpointEvent modEvent = (ModificationWatchpointEvent) event;
										System.out.println("old="
																			 + modEvent.valueCurrent());
										System.out.println("new=" + modEvent.valueToBe());
										System.out.println();
								} else if (event instanceof MethodEntryEvent) {
										MethodEntryEvent mee = (MethodEntryEvent) event;
										ReferenceType refType = mee.location().declaringType();
										try {
												if(refType instanceof ClassType) {
														mee.thread().suspend();
														ThreadReference threadReference = mee.thread();
														if(threadReference.frameCount() > 0) {
																StackFrame stackFrame = threadReference.frame(0);
																ObjectReference thisObject = stackFrame.thisObject();
																if(thisObject != null) {
																		Type type = thisObject.type();
																		System.out.println("Thread: " + type.name() + " Class: " + "((ClassType) refType) )" + " + Method: " + mee.method().name());
																}
														}
														mee.thread().resume();
												}
										} catch(IncompatibleThreadStateException itse) {
												itse.printStackTrace();
										}
								}
						}
						eventSet.resume();
				}
		}
		
		/** Watch all classes of name "Test" */
		private static void addClassWatch(VirtualMachine vm) {
				EventRequestManager erm = vm.eventRequestManager();
				ClassPrepareRequest classPrepareRequest = erm
						.createClassPrepareRequest();
				classPrepareRequest.addClassFilter(CLASS_NAME);
				classPrepareRequest.setEnabled(true);
		}
		
		private static void addMethodEntryRequest(VirtualMachine vm, ReferenceType refType) {
				System.out.println("Adding method entry request");
				EventRequestManager erm = vm.eventRequestManager();
				MethodEntryRequest mer = erm.createMethodEntryRequest();
				mer.addClassFilter("*");
				mer.setEnabled(true);
		}
		
}