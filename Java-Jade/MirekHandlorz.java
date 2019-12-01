package exchange;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.*;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;

import java.util.*;

public class MirekHandlorz extends Agent
{
	ArrayList<Pereleczki> lista = new ArrayList<Pereleczki>();
	
	
	Random rnd = new Random();
	int ilosc_samochodow_wystawionych=8;
	
	String nazwa="";
	//private MirekHandlorz m = new MirekHandlorz(); 
	
		// Put agent initializations here
		protected void setup() 
		{
			System.out.println("Witamy w AutoHandlu bêdziesz Pan zadowolony");
			// Register the book-selling service in the yellow pages
			DFAgentDescription dfd = new DFAgentDescription();
			dfd.setName(getAID());
			ServiceDescription sd = new ServiceDescription();
			sd.setType("niemiec-dzwonil-posluchac-silnika");
			sd.setName("Autohandel-by-Mirek-Handlorz");
			dfd.addServices(sd);
			try {
				DFService.register(this, dfd);
			}
			catch (FIPAException fe) {
				fe.printStackTrace();
			}
			nazwa = getAID().getName();
			
			// Add the behaviour serving queries from buyer agents
			addBehaviour(new OfferRequestsServer());

			// Add the behaviour serving purchase orders from buyer agents
			addBehaviour(new PurchaseOrdersServer());
			System.out.println(getAID().getName());
			
			for(int i=0; i<ilosc_samochodow_wystawionych; i++)
			{
				//int marka = rnd.nextInt(Pereleczki.POJAZDY.length-1);
				int marka = rnd.nextInt(4);
				int model = 1+rnd.nextInt(Pereleczki.POJAZDY[0].length-1);
				int nad = rnd.nextInt(Pereleczki.TYP_NADWOZIA.length-1);
				int pal = rnd.nextInt(Pereleczki.PALIWO.length-1);
				ArrayList<Pereleczki> pomocnicza_lista = new ArrayList<Pereleczki>();
				Pereleczki p = new Pereleczki(Pereleczki.POJAZDY[marka][0], Pereleczki.POJAZDY[marka][model], Pereleczki.TYP_NADWOZIA[nad], Pereleczki.PALIWO[pal], (1+rnd.nextInt(5))*1000, 2010+(rnd.nextInt(9)), (float)(rnd.nextInt(20)*1000), (float)rnd.nextInt(5000));
			//	Pereleczki p = new Pereleczki("Audi", Pereleczki.POJAZDY[marka][model], Pereleczki.TYP_NADWOZIA[nad], Pereleczki.PALIWO[pal], (1+rnd.nextInt(5))*1000, 2010+(rnd.nextInt(9)), (float)(rnd.nextInt(20)*1000), (float)rnd.nextInt(5000));
				lista.add(p); pomocnicza_lista.add(p);
				System.out.println(p);
			}
			System.out.println("--------------------------------------");
		}
		
		protected void takeDown() {
			// Deregister from the yellow pages
			try {
				DFService.deregister(this);
			}
			catch (FIPAException fe) {
				fe.printStackTrace();
			}
			// Close the GUI
			// Printout a dismissal message
			System.out.println("Skonczony deal "+getAID().getName());
		}
		
		private class OfferRequestsServer extends CyclicBehaviour {
			public void action() 
			{
				MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.CFP);
				ACLMessage msg = myAgent.receive(mt);
				
				if (msg != null) {
					// CFP Message received. Process it
					String title = msg.getContent();
				//	System.out.println("MSG: " + title);
					ACLMessage reply = msg.createReply();
					int flaga=0;
					System.out.println("SZUKANIE " + title);
			      
					for(Pereleczki p : lista) 
					{
				        if(p.marka != null && p.marka.contains(title))
						{
							reply.setPerformative(ACLMessage.PROPOSE);
							reply.setContent(p.toString());
							myAgent.send(reply);
					//		System.out.println("Czy wysyla ? " + reply.getContent());
							flaga+=1;
						}
					}
					
			//		System.out.println("Pokaz flare " + flaga);
					
					if(flaga==0)
					{
						// The requested car is NOT available for sale.
						reply.setPerformative(ACLMessage.REFUSE);
						reply.setContent("not-available");
						System.out.println("Jestem w wersji avaible oferty" + nazwa);
						System.out.println("Wyslano OfferRequestsServer REFUSE");
						myAgent.send(reply);	
					}
				//	System.out.println("zobaczymy czy duzo " + hmMarki.get("Audi"));
				    
				}
				else 
				{
					block();
				}
			}
		}
		
		private class PurchaseOrdersServer extends CyclicBehaviour {
			public void action() 
			{
				MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.ACCEPT_PROPOSAL);
				ACLMessage msg = myAgent.receive(mt);
		//		System.out.println("Pokaz mi wiadomosc " + msg);
				if (msg != null) {
					// ACCEPT_PROPOSAL Message received. Process it
					String txt = msg.getContent();
					ACLMessage reply = msg.createReply();
						// warunki tutaj wpisuje
					String[] parts = txt.split(", ");
					String title = parts[0];
					float cost = Float.valueOf(parts[1]);
					Pereleczki pereleczka = null;
					int pom_indeks=0;
					for(Pereleczki p : lista) 
					{
				        if(p.marka.contains(title) && p.cena_koncowa == cost)
						{
				        	pereleczka = p;
				        	pom_indeks = lista.indexOf(p);
						}
					}
					//System.out.println("pokaz p=" + pereleczka);
					if(pereleczka!=null)
					{
						if(!pereleczka.isRezerwacja()) //&& pereleczka.marka == title)
						{
							if(lista.get(pom_indeks) == pereleczka)
							{
								pereleczka.setRezerwacja(true);
								lista.set(pom_indeks, pereleczka);
								System.out.println("Zarezerwowal Janusz " + msg.getSender().getName());
							}
						}
						
							if(pereleczka.isRezerwacja())// && pereleczka.marka == title)
							{
								lista.remove(pereleczka);
								System.out.println(title+" kupil Janusz "+msg.getSender().getName() + " za " + pereleczka.cena_koncowa);
								reply.setPerformative(ACLMessage.INFORM);
								myAgent.send(reply);
							}
					}
				}
				else {
					block();
				}
				
			}
		}	
}


		
		
