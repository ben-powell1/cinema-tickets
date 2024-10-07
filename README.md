# Cinema tickets service

### Implementation notes

For a real implementation I would imagine that certain configuration such as ticket prices, maximum number of tickets 
etc. would likely be sourced from elsewhere rather than configured within the service. While I was tempted to look to 
separate out responsibilities here, my judgement was that the exercise was looking for a fairly simple implementation.

I've not added any surrounding bells and whistles e.g. REST endpoint to allow the service to be called. Again, this was
purely based on my assessment of the scope of the exercise.

My original implementation of TicketServiceImpl.getPriceForTicket used a Map<TicketTypeRequest.Type, Integer>, as I 
would typically prefer a declarative style for that type of config. However, the updated Switch now provides a 
nice clean syntax, avoids the potential for current ticket types to be omitted, and forces a future developer to cater for any 
new ticket types that are added.

The null checks for the varargs ticket requests are perhaps beyond what I'd normally expect, but I thought I'd go for 
maximum safety. It's also presumably unlikely that the method will be called with an array of requests which is 
subsequently mutated but I thought it was worth avoiding the risk.

I added invariants to TicketTypeRequest on the basis of failing as early as possible. Depending on the surrounding design
this may not be ideal e.g. if the requests were deserialised from a HTTP POST then perhaps it would be better to validate
separately.

I was intrigued that TicketService.purchaseTickets() declared that it throws InvalidPurchaseException which is an RTE,
and hence does not force a caller to handle the exception.

### Design concerns

If the payment service processes the payment but the seat reservation service fails to reserve seats, the customer will
have paid but not receive their reservation. For the purpose of this exercise I did not attempt to address this.