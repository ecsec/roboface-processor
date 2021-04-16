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

import java.util.Date;
import org.robovm.apple.foundation.NSObject;


/**
 *
 * @author Florian Otto
 */

public class SomeInterfaceImp extends NSObject implements SomeInterface{

	@Override
	public void simpleFun() {
        System.out.println("someFun was called");
	}

	@Override
	public void someFun(SomeEnum nameOfFirstParameter, String nameOfSecondParameter) {
		System.out.printf("someFun was called with params [%s;%s]", nameOfFirstParameter, nameOfSecondParameter);
	}

	@Override
	public SomeEnum giveEnumBack() {
		return SomeEnum.SecondValue;
	}

    @Override
    public Date getDate() {
	System.out.println("Returning now as date.");
	return new Date();
    }

    @Override
    public Date addOneDay(Date d) {
	return new Date(d.getTime() + 1000 * 60 * 60 * 24);
    }

}
