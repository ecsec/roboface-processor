/** **************************************************************************
 * Copyright (C) 2019 ecsec GmbH.
 * All rights reserved.
 * Contact: ecsec GmbH (info@ecsec.de)
 *
 * This file may be used in accordance with the terms and conditions
 * contained in a signed written agreement between you and ecsec GmbH.
 *
 ************************************************************************** */
package roboface.test;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Neil Crossley
 */
public class BasicSomeListEvaluator implements SomeArrayEvaluator {

	@Override
	public List<String> provideStrings() {
		List<String> result = new ArrayList<>();
		result.add("some stringy value");
		result.add("another one");
		result.add("the last string value");
		return result;
	}

	@Override
	public void acceptListIntegers(List<Integer> ints) {
		System.out.printf("BasicSomeArrayEvaluator has accepted integers: ");
		for (Integer anInt : ints) {
			System.out.print(anInt);
			System.out.print(',');
		}
		System.out.println(";");
	}

	@Override
	public void acceptSomeEnums(List<SomeEnum> enums) {
		System.out.printf("BasicSomeArrayEvaluator has accepted enums: ");
		for (SomeEnum aEnum : enums) {
			System.out.print(aEnum);
			System.out.print(',');
		}
		System.out.println(";");
	}

	@Override
	public List<SomeEnum> provideSomeEnums() {
		List<SomeEnum> result = new ArrayList<>();
		result.add(SomeEnum.SecondValue);
		result.add(SomeEnum.LastValue);
		result.add(SomeEnum.FirstValue);
		return result;
	}

}
