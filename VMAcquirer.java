import java.util.*;
import java.io.*;
import com.sun.jdi.*;
import com.sun.jdi.connect.*;

public class VMAcquirer {

		/**
		 * Call this with the localhost port to connect to.
		 */
		public VirtualMachine connect(int port)
				throws IOException {
				String strPort = Integer.toString(port);
				AttachingConnector connector = getConnector();
				try {
						VirtualMachine vm = connect(connector, strPort);
						return vm;
				} catch (IllegalConnectorArgumentsException e) {
						throw new IllegalStateException(e);
				}
		}

		private AttachingConnector getConnector() {
    VirtualMachineManager vmManager = Bootstrap
        .virtualMachineManager();
    for (Connector connector : vmManager
						 .attachingConnectors()) {
				System.out.println(connector.name());
				if ("com.sun.jdi.SocketAttach".equals(connector
																							.name())) {
						return (AttachingConnector) connector;
				}
    }
    throw new IllegalStateException();
		}

		private VirtualMachine connect(
																	 AttachingConnector connector, String port)
				throws IllegalConnectorArgumentsException,
							 IOException {
				Map<String, Connector.Argument> args = connector
						.defaultArguments();
				Connector.Argument pidArgument = args.get("port");
				if (pidArgument == null) {
						throw new IllegalStateException();
				}
				pidArgument.setValue(port);

				return connector.attach(args);
		}

}