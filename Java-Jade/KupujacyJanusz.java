package exchange;

import jade.core.Agent;
import jade.core.AID;
import jade.core.behaviours.*;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;

public class KupujacyJanusz extends Agent
{
	// The title of the book to buy
		private String targetAuto;
		private float kwota=30000;
		private int liczba_samochodow=3;
//	private int targetAuto;
		// The list of known seller agents
		private AID[] sellerAgents;
		private int ilesamochodow=0;
		
		// Put agent initializations here
		protected void setup() {
			// Printout a welcome message
			System.out.println("Hallo! Janusz zg³asza siê "+getAID().getName()+" jestem gotów.");
			// Get the title of the book to buy as a start-up argument
			Object[] args = getArguments();
			if (args != null && args.length > 0) {
				targetAuto = (String) args[0];
				System.out.println("Target " + targetAuto);
				kwota = Integer.parseInt((String) args[1]);
				liczba_samochodow = Integer.parseInt((String) args[2]);
				System.out.println("Auto bêdzie pere³eczk¹ " + targetAuto + " Kwota "+ kwota + " Liczba samochodów do zakupu " + liczba_samochodow);
//dodaj oferte ktora wysylasz stad mozna tak sprawdzic buyerow
				// Add a TickerBehaviour that schedules a request to seller agents every minute
				addBehaviour(new TickerBehaviour(this, 1000) {
					protected void onTick() {
						System.out.println("Kupujê "+targetAuto + " " + getAID().getName());
						// Update the list of seller agents
						DFAgentDescription template = new DFAgentDescription();
						ServiceDescription sd = new ServiceDescription();
						sd.setType("niemiec-dzwonil-posluchac-silnika");
						template.addServices(sd);
						try {
							DFAgentDescription[] result = DFService.search(myAgent, template); 
							System.out.println("Znaleziono Mirków Handlarzy:");
							sellerAgents = new AID[result.length];
							for (int i = 0; i < result.length; ++i) {
								sellerAgents[i] = result[i].getName();
								System.out.println(sellerAgents[i].getName());
							}
						}
						catch (FIPAException fe) {
							fe.printStackTrace();
						}

						// Perform the request
						myAgent.addBehaviour(new RequestPerformer());
					}
				} );
			}
			else {
				// Make the agent terminate
				System.out.println("Nie ma wystawionej per³eczki");
				doDelete();
			}
		}

		// Put agent clean-up operations here
		protected void takeDown() {
			// Printout a dismissal message
			System.out.println("Nic tu po mnie "+getAID().getName()+" wychodzê.");
		}

		/**
		   Inner class RequestPerformer.
		   This is the behaviour used by Book-buyer agents to request seller 
		   agents the target book.
		 */
		private class RequestPerformer extends Behaviour {
			private AID bestSeller,seller_before; // The agent who provides the best offer 
			private float bestPrice;  // The best offered price
			private int repliesCnt = 0; // The counter of replies from seller agents
			private MessageTemplate mt; // The template to receive replies
			private int step = 0;
			int reservation=0;
			
			public void action() 
			{
				
				switch (step) {
				case 0:
					// Send the cfp to all sellers
					ACLMessage cfp = new ACLMessage(ACLMessage.CFP);
					for (int i = 0; i < sellerAgents.length; ++i) {
						cfp.addReceiver(sellerAgents[i]);
					} 
					//cfp.setPerformative(targetAuto);
					cfp.setContent(targetAuto);
					cfp.setConversationId("auto-handel");
					cfp.setReplyWith("cfp"+System.currentTimeMillis()); // Unique value
					myAgent.send(cfp);
					// Prepare the template to get proposals
					mt = MessageTemplate.and(MessageTemplate.MatchConversationId("auto-handel"),
							MessageTemplate.MatchInReplyTo(cfp.getReplyWith()));
					step = 1;
					break;
				case 1:
					// Receive all proposals/refusals from seller agents
					ACLMessage reply = myAgent.receive(mt);
					//System.out.println("co to za reply" + reply);
					if (reply != null) {
						// Reply received
						if (reply.getPerformative() == ACLMessage.PROPOSE) {
							// This is an offer 
							String buffered = reply.getContent();
							buffered=buffered.replace("[", "");buffered=buffered.replace("]", "");
						//	System.out.println("Wyswietlam zawartosc wiadomosci");
							System.out.println(buffered);
							String[] parts = buffered.split(", ");
							String string1 = parts[8];
							//System.out.println("pokaz stringa " + parts[1]);
							float price = Float.valueOf(string1);
							seller_before = bestSeller;
							if (bestSeller == null || price < bestPrice) {
								// This is the best offer at present
								bestPrice = price;
								bestSeller = reply.getSender();
						//		System.out.println("Tak wygl¹da bestSeller " + bestSeller);
							}
						}
						repliesCnt++;
//						System.out.println("Liczba agentow: " + sellerAgents.length + " Ile razy policzyl: " + repliesCnt);
						if (repliesCnt >= sellerAgents.length) {
							// We received all replies
							step = 2; 
						}
					}
					else {
						block();
					}
					break;
				case 2:
					// Send the purchase order to the seller that provided the best offer
	         		if(bestSeller==seller_before)
						reservation = reservation + 1;
					ACLMessage order = new ACLMessage(ACLMessage.ACCEPT_PROPOSAL);
					order.addReceiver(bestSeller);
					order.setContent(targetAuto + ", " + String.valueOf(bestPrice));
					order.setConversationId("auto-handel");
					order.setReplyWith("order"+System.currentTimeMillis());
					//System.out.println("Pokaz mi czas " + order.getReplyWith());
					myAgent.send(order);
					// Prepare the template to get the purchase order reply
					mt = MessageTemplate.and(MessageTemplate.MatchConversationId("auto-handel"),
							MessageTemplate.MatchInReplyTo(order.getReplyWith()));

					if(reservation>1)
						step = 3;
					break;
				case 3:      
					// Receive the purchase order reply
					reply = myAgent.receive(mt);
//					System.out.println("Reply " + reply);
					if (reply != null) {
						// Purchase order reply received
						if (reply.getPerformative() == ACLMessage.INFORM) {
							// Purchase successful. We can terminate
							ilesamochodow++;
							System.out.println(targetAuto+" kupi³ Janusz "+getAID().getName());
							kwota = kwota - bestPrice;
//							System.out.println("Pokaz mi bestaprice " + bestPrice);
							if(kwota>0)
							{
								System.out.println("Agent kupuje " + getAID().getName() + " Koszt = "+bestPrice + ". Po odliczeniu " + kwota + " samochod nr " + ilesamochodow);
							}
							else
							{
								System.out.println("Za du¿e wydatki " + getAID().getName());
								step=4;
							}
//							ACLMessage order1 = new ACLMessage(ACLMessage.ACCEPT_PROPOSAL);
	//						order1.addReceiver(bestSeller);
		//					order1.setContent(targetAuto);
			//				order1.setConversationId("auto-handel");
				//			order1.setReplyWith("order"+System.currentTimeMillis());
					//		myAgent.send(order1);
						//	mt = MessageTemplate.and(MessageTemplate.MatchConversationId("auto-handel"),
							//		MessageTemplate.MatchInReplyTo(order1.getReplyWith()));
							
							if (ilesamochodow >= liczba_samochodow)
							{
								myAgent.doDelete();
								System.out.println("Ile kupiono " + ilesamochodow);
							}
						}
						else {
							System.out.println("Treœæ " + reply);
							System.out.println("Próba nie powiod³a siê: ¿¹dana pere³eczka zosta³a ju¿ sprzedana lub kwota przewyzsza zakup. Kwota aktualna " + kwota);
						}

						step = 4;
					}
					else {
						block();
					}
					break;
				}        
			}

			public boolean done() {
				if (step == 2 && bestSeller == null) {
					System.out.println("Próba nie powiod³a siê: "+targetAuto+" pere³eczka nie dostêpna");
				}
				return ((step == 2 && bestSeller == null) || step == 4);
			}
		}  // End of inner class RequestPerformer
}
