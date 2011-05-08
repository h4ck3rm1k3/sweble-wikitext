/**
 * Copyright 2011 The Open Source Research Group,
 *                University of Erlangen-Nuernberg
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.sweble.wikitext.lazy;

import static junit.framework.Assert.assertEquals;

import java.io.IOException;

import org.junit.Test;
import org.sweble.wikitext.lazy.encval.IllegalCodePoint;
import org.sweble.wikitext.lazy.encval.IllegalCodePointType;

import de.fau.cs.osr.ptk.common.EntityMap;
import de.fau.cs.osr.ptk.common.ast.Location;

public class EncodingValidatorTest
{
	@Test
	public void testEncodingValidator() throws IOException
	{
		String title = "dummy";
		
		StringBuilder source = new StringBuilder();
		source.append("Ein einfacher Test-String\n"); // L 0
		source.append("mit ein paar \uE800 und \r\n"); // L 1:13
		source.append("natuerlich ein paar \uFDEE.\n"); // L 2:19
		source.append("Aber auch \uDBEF und \uDC80 \r"); // L 3:10, 3:16
		source.append("duerfen nicht fehlen. Zu guter \n");// L4
		source.append("Letzt noch ein Wohlklang \u0007."); // L 5:25
		
		/* Ruins the test string!
		InputStreamReader in = new InputStreamReader(
		        IOUtils.toInputStream(source.toString(), "UTF-8"));
		*/

		/*
		StringReader in = new StringReader(source.toString());
		while (true)
		{
			int c = in.read();
			if (c == -1)
				break;
			
			System.out.format("%c: U+%04x\n", c, (int) c);
		}
		in.close();
		*/

		EntityMap entityMap = new EntityMap();
		
		LazyEncodingValidator v = new LazyEncodingValidator();
		String result = v.validate(source.toString(), title, entityMap);
		
		IllegalCodePoint x0 = (IllegalCodePoint) entityMap.getEntity(0);
		assertEquals("\uE800", x0.getCodePoint());
		assertEquals(IllegalCodePointType.PRIVATE_USE_CHARACTER, x0.getType());
		assertEquals(new Location(title, 1, 13), x0.getNativeLocation());
		
		IllegalCodePoint x1 = (IllegalCodePoint) entityMap.getEntity(1);
		assertEquals("\uFDEE", x1.getCodePoint());
		assertEquals(IllegalCodePointType.NON_CHARACTER, x1.getType());
		assertEquals(new Location(title, 2, 19), x1.getNativeLocation());
		
		IllegalCodePoint x2 = (IllegalCodePoint) entityMap.getEntity(2);
		assertEquals("\uDBEF", x2.getCodePoint());
		assertEquals(IllegalCodePointType.ISOLATED_SURROGATE, x2.getType());
		assertEquals(new Location(title, 3, 10), x2.getNativeLocation());
		
		IllegalCodePoint x3 = (IllegalCodePoint) entityMap.getEntity(3);
		assertEquals("\uDC80", x3.getCodePoint());
		assertEquals(IllegalCodePointType.ISOLATED_SURROGATE, x3.getType());
		assertEquals(new Location(title, 3, 16), x3.getNativeLocation());
		
		IllegalCodePoint x4 = (IllegalCodePoint) entityMap.getEntity(4);
		assertEquals("\u0007", x4.getCodePoint());
		assertEquals(IllegalCodePointType.CONTROL_CHARACTER, x4.getType());
		assertEquals(new Location(title, 5, 25), x4.getNativeLocation());
		
		StringBuilder ref = new StringBuilder();
		ref.append("Ein einfacher Test-String\n");
		ref.append("mit ein paar \uE0000\uE001 und \r\n");
		ref.append("natuerlich ein paar \uE0001\uE001.\n");
		ref.append("Aber auch \uE0002\uE001 und \uE0003\uE001 \r");
		ref.append("duerfen nicht fehlen. Zu guter \n");
		ref.append("Letzt noch ein Wohlklang \uE0004\uE001.");
		
		assertEquals(ref.toString(), result);
	}
}
