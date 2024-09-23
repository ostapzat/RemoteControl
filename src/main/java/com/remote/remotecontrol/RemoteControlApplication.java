package com.remote.remotecontrol;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.URL;
import java.util.Enumeration;

@SpringBootApplication
public class RemoteControlApplication {

	public static void main(String[] args) {
		SpringApplication.run(RemoteControlApplication.class, args);

		String ipAddress = getLocalIpAddress();

		String connectionUrl = "http://" + ipAddress + ":8080";

		sendToDiscordWebhook(connectionUrl);
	}

	private static String getLocalIpAddress() {
		try {
			Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
			while (interfaces.hasMoreElements()) {
				NetworkInterface networkInterface = interfaces.nextElement();
				Enumeration<InetAddress> addresses = networkInterface.getInetAddresses();

				while (addresses.hasMoreElements()) {
					InetAddress inetAddress = addresses.nextElement();
					if (!inetAddress.isLoopbackAddress() && inetAddress.isSiteLocalAddress()) {
						return inetAddress.getHostAddress();
					}
				}
			}
		} catch (Exception e) {
			System.out.println("Could not get the local IP address: " + e.getMessage());
		}

		return "127.0.0.1";
	}

	private static void sendToDiscordWebhook(String connectionUrl) {
		String webhookUrl = "https://discordapp.com/api/webhooks/1285586965370241065/iIqwwlTvaMYn4y_8Eaj73nnGgICk7-jFSLaQKrj077IweS9O91nedXnX-OVvFKqRec6Q";
		String message = "{\"content\":\"Join the address: " + connectionUrl + "\"}";

		try {
			URL url = new URL(webhookUrl);
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("POST");
			connection.setRequestProperty("Content-Type", "application/json");
			connection.setDoOutput(true);

			try (OutputStream os = connection.getOutputStream()) {
				byte[] input = message.getBytes("utf-8");
				os.write(input, 0, input.length);
			}

			try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"))) {
				StringBuilder response = new StringBuilder();
				String responseLine;
				while ((responseLine = br.readLine()) != null) {
					response.append(responseLine.trim());
				}
				System.out.println("Response from Discord: " + response.toString());
			}

		} catch (Exception e) {
			System.out.println("Could not send the message to the webhook: " + e.getMessage());
		}
	}
}
