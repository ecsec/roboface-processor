/** **************************************************************************
 * Copyright (C) 2020 ecsec GmbH.
 * All rights reserved.
 * Contact: ecsec GmbH (info@ecsec.de)
 *
 * This file may be used in accordance with the terms and conditions
 * contained in a signed written agreement between you and ecsec GmbH.
 *
 ************************************************************************** */
package roboface.test.inheritance;

import org.openecard.robovm.annotations.FrameworkInterface;

/**
 *
 * @author Neil Crossley
 */
@FrameworkInterface
public interface ChildInterface extends ParentInterface {

	void someChildMethod();

	void someOtherChildMethod(int x);
}
