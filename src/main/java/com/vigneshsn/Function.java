package com.vigneshsn;

import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.HttpMethod;
import com.microsoft.azure.functions.HttpRequestMessage;
import com.microsoft.azure.functions.HttpResponseMessage;
import com.microsoft.azure.functions.HttpStatus;
import com.microsoft.azure.functions.OutputBinding;
import com.microsoft.azure.functions.annotation.AuthorizationLevel;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.HttpTrigger;
import com.microsoft.azure.functions.annotation.QueueOutput;
import com.microsoft.azure.functions.annotation.QueueTrigger;

import java.math.BigDecimal;
import java.util.Random;

/**
 * Azure Functions with HTTP Trigger.
 */
public class Function {

    Random random = new Random();
    /**
     * This function listens at endpoint "/api/OfferService"
     * curl -d "HTTP Body" {your host}/api/OfferService
     */
    @FunctionName("OfferService")
    public HttpResponseMessage checkOffer(
            @HttpTrigger(
                name = "req",
                methods = {HttpMethod.POST},
                authLevel = AuthorizationLevel.ANONYMOUS) HttpRequestMessage<Order> request,
                @QueueOutput(name = "offers", queueName = "offers", connection = "OfferQueueConnectionString") OutputBinding<Offer> offerQueue,
            final ExecutionContext context) {

        context.getLogger().info("Java HTTP trigger processed a request.");
        final Order order = request.getBody();

        String reason = "offer not applicable";
        if ( isOrderEligibleForOffers(order) )  {
            reason = "Offer applicable";
            context.getLogger().info("message added to queue for user "+ order.getUsername());
            Offer offer = getOffer(order.getUsername());
            offerQueue.setValue(offer);
        }
        context.getLogger().info("--------------------------------------------------");
        return request.createResponseBuilder(HttpStatus.OK).body(reason).build();
    }

    /**
     * This functions is triggered by the Offer queue
     * @param offer
     * @param context
     */
    @FunctionName("SendOfferService")
    public void sendOffer(
            @QueueTrigger(name = "offers", queueName = "offers", connection = "OfferQueueConnectionString") Offer offer,
            final ExecutionContext context
    ) {
        context.getLogger().info("Hey! " +offer.getEmail()+ " you have a offer code XXXX" );
    }


    boolean isOrderEligibleForOffers(Order order) {
        return order.getAmount().compareTo(BigDecimal.valueOf(500)) > 0;
    }

    Offer getOffer(String email) {
        return new Offer(email, random.nextInt(30));
    }
}

class Order {
    private String username;
    private BigDecimal amount;

    public Order() {
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
}

class Offer {

    private String email;
    private int discount;

    public int getDiscount() {
        return discount;
    }

    public Offer(String email, int discount) {
        this.email = email;
        this.discount = discount;
    }

    public void setDiscount(int discount) {
        this.discount = discount;
    }

    public Offer() {
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}

