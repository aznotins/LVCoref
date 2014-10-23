/*******************************************************************************
 * Copyright 2013,2014 Institute of Mathematics and Computer Science, University of Latvia
 * Author: Artūrs Znotiņš
 * 
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 * 
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 * 
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *******************************************************************************/
package LVCoref;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Properties;

import org.junit.BeforeClass;
import org.junit.Test;


public class LVCorefTest {
	private static String propertiesFile = "lvcoref.prop";
	private static Properties props;

	public static void assertLink(Document d, int idx1, int idx2) {
		Node x = d.getNode(idx1);
		Node y = d.getNode(idx2);
		assertNotNull("\""+x.word +"\""+ " not set as a mention", x.mention);
		assertNotNull("\""+y.word +"\""+ " not set as a mention", y.mention);
		assertEquals("Mentions not in same cluster" + "(" + x.word + ", " +y.word + ")", x.mention.corefClusterID, y.mention.corefClusterID);
	}
	
	@BeforeClass
    public static void oneTimeSetUp() throws IOException, ClassCastException, ClassNotFoundException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
		Properties props = new Properties();
		props.load(new FileInputStream(propertiesFile));
		props.setProperty(Constants.OUTPUT_PROP, "none");
		LVCoref.initializeProperties(props);		
    }

	@Test
	
	public void test1() throws Exception {		
		Document doc = new Document(LVCoref.dictionaries);
		doc.readCONLL("sample1_ner.conll");
		LVCoref.processDocument(doc);
		doc.printSimpleText(System.err);
		assertLink(doc, 1, 3); // predikatīvais nominatīvs
		assertLink(doc, 1, 5); // vietniekvārds "viņš"
		assertLink(doc, 1, 6); // vietniekvārds "savs"
	}

}
