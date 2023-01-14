package jadelab2;

import jade.core.Agent;
import jade.core.behaviours.*;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;

import java.util.*;

public class BookSellerAgent extends Agent {
  private HashMap<String, Book> catalogue;
  private BookSellerGui myGui;

  protected void setup() {
    catalogue = new HashMap<>();
    myGui = new BookSellerGui(this);
    myGui.display();

    //book selling service registration at DF
    DFAgentDescription dfd = new DFAgentDescription();
    dfd.setName(getAID());
    ServiceDescription sd = new ServiceDescription();
    sd.setType("book-selling");
    sd.setName("JADE-book-trading");
    dfd.addServices(sd);
    try {
      DFService.register(this, dfd);
    }
    catch (FIPAException fe) {
      fe.printStackTrace();
    }
    
    addBehaviour(new OfferRequestsServer());

    addBehaviour(new PurchaseOrdersServer());
  }

  protected void takeDown() {
    //book selling service deregistration at DF
    try {
      DFService.deregister(this);
    }
    catch (FIPAException fe) {
      fe.printStackTrace();
    }
  	myGui.dispose();
    System.out.println("Seller agent " + getAID().getName() + " terminated.");
  }

  //invoked from GUI, when a new book is added to the catalogue
  public void updateCatalogue(final String title, final int price, final int shipmentPrice) {
    addBehaviour(new OneShotBehaviour() {
      public void action() {
		  var bookId = UUID.randomUUID().toString();
		catalogue.put(bookId, new Book(bookId, title, price, shipmentPrice));
		System.out.println(getAID().getLocalName() + ": " + title + " put into the catalogue. Price = " + price + " Shipment price = " + shipmentPrice);
      }
    } );
  }
  
	private class OfferRequestsServer extends CyclicBehaviour {
	  public void action() {
	    //proposals only template
		MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.CFP);
		ACLMessage msg = myAgent.receive(mt);
	    if (msg != null) {
	      String title = msg.getContent();
	      ACLMessage reply = msg.createReply();

		  System.out.println("Searching book in catalogue...");
		  var searchedBooks = new ArrayList<Book>();
		  var searchedBook = new Book(null, null, 0, 0);

		  for(String key: catalogue.keySet()){
			  var book = catalogue.get(key);
			  if(book.getTitle().equals(title)){
				  searchedBook = book;
				  System.out.println("Following book has been found. Title: " + book.Title + " Price: " + book.getFullPrice()
				  + " BookID: " + book.getID());
				  break;
			  }
		  }

		  if(searchedBook.getPrice() != 0){
			  reply.setPerformative(ACLMessage.PROPOSE);
			  reply.setContent(String.valueOf(searchedBook.getFullPrice()));
			  reply.setOntology(searchedBook.getID());
		  }
		  else{
			  reply.setPerformative(ACLMessage.REFUSE);
			  reply.setContent("not-available");
		  }
		  myAgent.send(reply);
	    }
	    else {
	      block();
	    }
	  }
	}

	
	private class PurchaseOrdersServer extends CyclicBehaviour {
	  public void action() {
	    //purchase order as proposal acceptance only template
		MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.ACCEPT_PROPOSAL);
		ACLMessage msg = myAgent.receive(mt);
	    if (msg != null) {
	      String title = msg.getContent();
	      ACLMessage reply = msg.createReply();

		  Integer price = null;

		  var bookId = msg.getOntology();
		  for(String key: catalogue.keySet()) {
			  var book = catalogue.get(key);
			  if (book.getID().equals(bookId)) {
				  price = book.getPrice();
				  System.out.println("Following book has been removed from catalogue. Title: " + book.Title + " Price: " + book.getFullPrice()
						  + " BookID: " + book.getID());
				  catalogue.remove(bookId);
				  break;
			  }
		  }

	      if (price != null || price != 0) {
	        reply.setPerformative(ACLMessage.INFORM);
	        System.out.println(getAID().getLocalName() + ": " + title + " sold to " + msg.getSender().getLocalName());
	      }
	      else {
	        //title not found in the catalogue, sold to another agent in the meantime (after proposal submission)
	        reply.setPerformative(ACLMessage.FAILURE);
	        reply.setContent("not-available");
	      }
	      myAgent.send(reply);
	    }
	    else {
		  block();
		}
	  }
	}

}
