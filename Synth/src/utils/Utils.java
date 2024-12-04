package utils;

public class Utils {
public static void invokeProcedure (Procedure procedure, boolean printTrace) {
	try {
		procedure.invoke();
	}
	catch (Exception e) {
		if (printTrace) {
			e.printStackTrace();
		}
	}
}
}
