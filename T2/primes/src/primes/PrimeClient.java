package primes;

import java.net.URI;
import java.util.HashSet;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriBuilder;

import org.glassfish.jersey.client.ClientConfig;

public class PrimeClient {
	public static void main(String[] args) {
		new PrimeClient().run();
	}
	
	private static String PATH_PRIME = "PrimeCollection";
	private static String PATH_COMPOSITE = "CompositeCollection";
	
	private WebTarget webTarget;

	public PrimeClient() {
		ClientConfig config = new ClientConfig();
		Client client = ClientBuilder.newClient(config);
		
		URI uriPrime = UriBuilder
				.fromUri("http://localhost:8080/primes/rest/")
				.build();
		
		webTarget = client.target(uriPrime);
	}

	private void run() {
		testValue(7);
		
		testValue(7);
		
		testValue(12);
		
		testValue(12);
		
		testValue(15);
		
		testValue(101);
		
		testValue(1001347);
	}

	private void testValue(long value) {
		boolean existsPrime = checkServerForPrime(value);
		boolean existsComposite = checkServerForComposite(value);

		System.out.println(
				String.format("Prime collection contains %o: %b", value, existsPrime));
		
		System.out.println(
				String.format("Composite collection contains %o: %b", value, existsComposite));
		
		addNumberToServer(value);
		
		System.out.println("");
	}
	
	

	private void addNumberToServer(long value) {
		String target = isPrime(value) ? PATH_PRIME : PATH_COMPOSITE;
		
		Response response = webTarget
				.path(target)
				.path("add")
        		.path("" + value)
        		.request()
        		.post(Entity.entity(value, MediaType.TEXT_PLAIN));
		
		String r = response.readEntity(String.class);
		System.out.println(r);
	}
	
	private boolean checkServerForComposite(long value) {
		Response response = webTarget
				.path(PATH_COMPOSITE)
				.path("contains")
        		.path("" + value)
        		.request()
        		.get();
		
		if (response.getStatusInfo().toEnum() != Status.OK) {
			System.out.println("error!");
			return false;
		}
		
		Boolean r = response.readEntity(Boolean.class);	
		return r;
	}
	
	private boolean checkServerForPrime(long value) {
		Response response = webTarget
				.path(PATH_PRIME)
				.path("contains")
        		.path("" + value)
        		.request()
        		.get();
		
		if (response.getStatusInfo().toEnum() != Status.OK) {
			System.out.println("error!");
			return false;
		}
		
		Boolean r = response.readEntity(Boolean.class);	
		return r;
	}

	/*
     * Check if a number is a primer number.
     * @author Oscar_Sanchez, https://www.mkyong.com/java/how-to-determine-a-prime-number-in-java/
     */
    private boolean isPrime(long n) {
        //check if n is a multiple of 2
        if (n % 2 == 0) {
            return false;
        }

        //if not, then just check the odds
        for (long i = 3; i * i <= n; i += 2) {
            if (n % i == 0) {
                return false;
            }
        }
        return true;
    }
	
	/*
	 * Get number response from server. Show if number is prime and if it was
	 * fetched from a lookup table.
	 * 
	 * @param value to check
	 * 
	 * @return value and details in XML format.
	 */
	/*
	private String checkIfPrime(long value) {
		Response response = webTargetPrime
				.path("isPrime")
				.path("" + value)
				.request()
				.get();
		
		String s = response.readEntity(String.class);
		return s;
	}
	*/
}
