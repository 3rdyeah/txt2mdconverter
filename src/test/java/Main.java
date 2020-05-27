import java.util.Scanner;

public class Main {

	public static void main(String[] args) throws Exception {
		String input = "";
		Converter converter = new Converter();
		while (!input.equalsIgnoreCase("exit")) {
			System.out.println("Input a path of file or folder(input exit to quit):");
			Scanner scanner = new Scanner(System.in);
			input = scanner.next();
			if (!input.equalsIgnoreCase("exit")) {
				converter.conv(input);
			}
		}
	}
}
