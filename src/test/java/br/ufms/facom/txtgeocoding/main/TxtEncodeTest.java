package br.ufms.facom.txtgeocoding.main;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

public class TxtEncodeTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {

	}


	@Test
	public void testGetGeoTag() {
		TxtEncode encode = new TxtEncode("", "");
		String [] coords = encode.getGeoTag("Salvador");
		assertEquals(coords.length, 2);
		
	}

	@Test
	public void testSplitTerms(){
		String event = "Assalto a mão armada em São Luis do Maranhão";
		TxtEncode encode = new TxtEncode("", "");
		String [] terms = encode.splitTerms(event);
		assertEquals(terms.length,9);
		System.out.println(Arrays.toString(terms));
	}
	
	@Test
	public void testProcessEvent(){
		String event = "Assalto a mão armada em São Luis do Maranhão";
		TxtEncode encode = new TxtEncode("", "");
		String [] coords = encode.processEvent(event);
		assertEquals(coords.length, 2);
		System.out.println(Arrays.toString(coords));
	}
	
	@Test
	public void testEncode() throws IOException, Exception{
		TxtEncode encode = new TxtEncode("/Users/ronaldoflorence/Documents/Mestrado/Dissertacao/seguranca_publica/txt/", "/Users/ronaldoflorence/Documents/Mestrado/Dissertacao/seguranca_publica/geotags.csv");
		List<List<String>> result = encode.encode();
		System.out.println(result);
	}
}
