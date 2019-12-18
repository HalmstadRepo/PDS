package primes;

import java.util.HashSet;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

public class NumberCollection {
	protected HashSet<Long> set;

	public NumberCollection() {
		set = new HashSet<Long>();
	}
	
	@POST
    @Path("/add/{value}")
    public Response createUser(@PathParam("value") long value) {
		boolean contains = set.contains(value);
		
		set.add(value);
		
		String s = contains ? String.format("Collection already contains %o", value) 
				: "Value added";
		
		return Response
				.ok(s, MediaType.TEXT_PLAIN)
				.build();
	}
	
	@GET
    @Path("/contains/{value}")
    public Response prime(@PathParam("value") long value) {
    	boolean contains = set.contains(value);

    	return Response
				.ok(contains, MediaType.TEXT_PLAIN)
				.build();
    }
}
