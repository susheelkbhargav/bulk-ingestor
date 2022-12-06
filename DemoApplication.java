package graphtutorial;

import com.microsoft.graph.models.User;

import java.io.IOException;
import java.util.InputMismatchException;
import java.util.Properties;
import java.util.Scanner;


public class App {
	// <MainSnippet>
	public static void main(String[] args) {
		System.out.println("Java Graph Tutorial");
		System.out.println();

		final Properties oAuthProperties = new Properties();
		try {
			oAuthProperties.load(graphtutorial.App.class.getResourceAsStream("oAuth.properties"));
		} catch (IOException e) {
			System.out.println("Unable to read OAuth configuration. Make sure you have a properly formatted oAuth.properties file. See README for details.");
			return;
		}

		initializeGraph(oAuthProperties);

		greetUser();

		Scanner input = new Scanner(System.in);

		int choice = -1;

		while (choice != 0) {
			System.out.println("Please choose one of the following options:");
			System.out.println("0. Exit");
			System.out.println("1. Display access token");
			System.out.println("2. Make a Graph call");
			System.out.println("3. List Files in OneDrive");

			try {
				choice = input.nextInt();
			} catch (InputMismatchException ex) {
				// Skip over non-integer input
			}

			input.nextLine();

			// Process user choice
			switch(choice) {
				case 0:
					// Exit the program
					System.out.println("Goodbye...");
					break;
				case 1:
					// Display access token
					displayAccessToken();
					break;
				case 2:
					// Run any Graph code
					makeGraphCall();
					break;
				case 3:
					// Run any Graph code
					listFiles();
					break;
				default:
					System.out.println("Invalid choice");
			}
		}

		input.close();
	}

	private static void initializeGraph(Properties properties) {
		try {
			Graph.initializeGraphForUserAuth(properties,
					challenge -> System.out.println(challenge.getMessage()));
		} catch (Exception e)
		{
			System.out.println("Error initializing Graph for user auth");
			System.out.println(e.getMessage());
		}
	}
	// </InitializeGraphSnippet>

	private static void greetUser() {
		try {
			final User user = Graph.getUser();
			// For Work/school accounts, email is in mail property
			// Personal accounts, email is in userPrincipalName
			final String email = user.mail == null ? user.userPrincipalName : user.mail;
			System.out.println("Hello, " + user.displayName + "!");
			System.out.println("Email: " + email);
		} catch (Exception e) {
			System.out.println("Error getting user");
			System.out.println(e.getMessage());
		}
	}

	private static void displayAccessToken() {
		try {
			final String accessToken = Graph.getUserToken();
			System.out.println("Access token: " + accessToken);
		} catch (Exception e) {
			System.out.println("Error getting access token");
			System.out.println(e.getMessage());
		}
	}

	private static void listFiles() {
		try {
			System.out.println("\nInvoking listFiles() on OneDrive");
			Graph.listFiles();

		} catch (Exception e) {
			System.out.println("Error sending mail");
			System.out.println(e.getMessage());
		}
	}

	private static void makeGraphCall() {
		try {
			Graph.makeGraphCallToGetAllFiles();
		} catch (Exception e) {
			System.out.println("Error making Graph call");
			System.out.println(e.getMessage());
		}
	}
}
