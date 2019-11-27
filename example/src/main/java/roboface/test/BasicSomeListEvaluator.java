/****************************************************************************
 * Copyright (C) 2019 ecsec GmbH.
 * All rights reserved.
 * Contact: ecsec GmbH (info@ecsec.de)
 *
 * This file is part of the Open eCard App.
 *
 * GNU General Public License Usage
 * This file may be used under the terms of the GNU General Public
 * License version 3.0 as published by the Free Software Foundation
 * and appearing in the file LICENSE.GPL included in the packaging of
 * this file. Please review the following information to ensure the
 * GNU General Public License version 3.0 requirements will be met:
 * http://www.gnu.org/copyleft/gpl.html.
 *
 * Other Usage
 * Alternatively, this file may be used in accordance with the terms
 * and conditions contained in a signed written agreement between
 * you and ecsec GmbH.
 *
 ***************************************************************************/
package roboface.test;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Neil Crossley
 */
public class BasicSomeListEvaluator implements SomeArrayEvaluator {

	@Override
	public void acceptListStrings(List<String> vals) {
		System.out.printf("BasicSomeListEvaluator has accepted strings: ");
		for (String aString : vals) {
			System.out.print(aString);
			System.out.print(',');
		}
		System.out.println(";");
	}

	@Override
	public List<String> provideStringsFromByteBuffer(
			ByteBuffer bytes,
			ByteBuffer secondBytes,
			ByteBuffer thirdBytes,
			ByteBuffer lastBytes) {
		System.out.printf("BasicSomeListEvaluator is providing Strings given bytes: ");
		printByteArray(bytes);
		printByteArray(secondBytes);
		printByteArray(thirdBytes);
		printByteArray(lastBytes);
		List<String> result = new ArrayList<>();
		result.add("some stringy value");
		result.add("another one");
		result.add("the last string value");
		return result;
	}

	private void printByteArray(ByteBuffer bytes) {
		byte[] values = bytes.array();
		for (int i = 0; i < values.length; i++) {
			byte aByte = values[i];
			System.out.print(aByte);
			System.out.print(',');
		}
		System.out.println(";");
	}

	@Override
	public ByteBuffer provideAByteBuffer() {
		byte[] byteValue = new byte[] {
			-3,-2,-1,0,1,2,3,
		};
		return ByteBuffer.wrap(byteValue);
	}


}
