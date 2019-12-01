package exchange;

import java.util.Random;

public class Pereleczki 
{
	String marka, model, typ_nadwozia, paliwo;
	float cena, cena_dodatkowa, cena_koncowa;
	int rok, pojemnosc;
	
	static final String[][] POJAZDY = {
			{"BMW","X3","M5","E46", "i3"},
			{"Opel", "Corsa", "Astra", "Insignia", "Grandland"},
			{"Audi", "A4", "TT", "Q3", "RS8"},
			{"Volvo", "S40", "V50", "XC90", "S80"},
			{"Toyota", "Celica", "Supra", "GT86", "Corolla"},
			{"Chevrolet" ,"Chevelle" , "Corvette" , "Camaro" , "Volt"},
			{"Honda", "Civic" , "S2000" , "CR-V" , "Accord"},
			{"Fiat","Punto","Bravo","Tipo", "500"},
			{"Skoda","Superb","Fabia","Octavia","Kodiaq"},
			{"VW","Passat","Golf","Phaeton", "Touareg"},
			{"Pontiac", "GTO" , "G8" , "Torrent", "Catalina"},
			{"Dodge","Charger" , "Challenger" , "Viper" ,"Caliber"},
			{"Maserati","3200GT","Ghibli","Quattroporte","GranTurismo"},
			{"Ford","Mustankg","Focus","Mondeo","GT"},
			{"Bentley","GT Supersport","Continental","Bentayga","Mulsanne"}
			};
	static final String[] TYP_NADWOZIA = {"Combi","Hatchback","Sedan","Compact","Coupe"}; 
	static final String[] PALIWO = {"Benzyna", "Diesel"};

	boolean rezerwacja = false;
	
	public Pereleczki(String marka, String model, String typ_nadwozia, String paliwo, int pojemnosc, int rok, float cena, float cena_dodatkowa) 
	{
		super();
		this.marka = marka;
		this.model = model;
		this.typ_nadwozia = typ_nadwozia;
		this.paliwo = paliwo;
		this.cena = cena;
		this.rok = rok;
		this.pojemnosc = pojemnosc;
		this.cena_dodatkowa = cena_dodatkowa;
		this.cena_koncowa = cena_dodatkowa + cena;
	//	this.rezerwacja = false;
	}

	public boolean isRezerwacja()
	{
		return rezerwacja;
	}
	
	
	public void setRezerwacja(boolean rezerwacja) 
	{
		this.rezerwacja = rezerwacja;
	}
	
	@Override
	public String toString() {
		return marka + ", " + model + ", " + typ_nadwozia + ", " + paliwo + ", " + pojemnosc + ", " + rok + ", " + cena + ", " + cena_dodatkowa + ", " + cena_koncowa + ", " + rezerwacja;
		//return "Pojazd [marka=" + marka + ", model=" + model + ", nadwozie=" + typ_nadwozia + ", paliwo=" + paliwo + ", pojemnosc=" + pojemnosc  +", rok=" + rok + ", cena=" + cena + ", koszta dodatkowe=" + cena_dodatkowa + ", cena ostateczna=" + cena_koncowa + "]";
	}
		
}
