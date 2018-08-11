package br.ufms.facom.txtgeocoding;

import br.ufms.facom.txtgeocoding.main.TxtEncode;

public class TxtgeocodingApplication {

	public static void main(String[] args) {
		TxtEncode encode = new TxtEncode(args[0], args[1]);
		try {
			encode.encode();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
}
