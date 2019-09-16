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

import org.openecard.robovm.annotations.FrameworkObject;


/**
 *
 * @author Tobias Wich
 * @author Florian Otto
 */

@FrameworkObject(factoryMethod = "createFramework")
public class MyFrameworkImplementation implements MyFrameworkInterface {

	//some functions
	public void fun(){
		System.out.println("fun was called");
	}
	public void fun(String s){
		System.out.println("overloaded fun was called given " + s);
	}
	public void otherfun(int i){
		System.out.println("otherfun was called given " + i);
	}

	//getter for other implementations
	public SomeInterface getSomeImp(){
		return new SomeInterfaceImp();
	}
	public SomeInterface.SomeInnerInterface getSomeInnerImp(){
		return new SomeInnerImp();
	}

}
