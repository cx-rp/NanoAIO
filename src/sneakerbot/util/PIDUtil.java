package sneakerbot.util;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import sun.management.VMManagement;

@SuppressWarnings("restriction")
public final class PIDUtil {
	private PIDUtil() {
	}

	public static int getPID() {
		try {
			return getPIDByRuntimeName();
		} catch (NumberFormatException e) {
			try {
				return getPIDForOracleJVM();
			} catch(Throwable t) {
				throw new UnsupportedOperationException("this JVM does not support PID retrieval");
			}
		}
	}

	protected static int getPIDByRuntimeName() throws NumberFormatException {
		RuntimeMXBean runtime = ManagementFactory.getRuntimeMXBean();
		String pidString = runtime.getName().split("@")[0];
		
		return Integer.parseInt(pidString);
	}

	protected static int getPIDForOracleJVM() throws Exception {
		RuntimeMXBean runtime = ManagementFactory.getRuntimeMXBean();
		Field jvm = runtime.getClass().getDeclaredField("jvm");
		jvm.setAccessible(true);
		VMManagement mgmt = (VMManagement) jvm.get(runtime);
		Method getProcessId = mgmt.getClass().getDeclaredMethod("getProcessId");
		getProcessId.setAccessible(true);

		return (Integer) getProcessId.invoke(mgmt);
	}
}