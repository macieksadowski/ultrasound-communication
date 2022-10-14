package ultrasound.encoder;

import ultrasound.ICoderBuilder;

/**
 * Builder for new instances {@link IEncoder}
 */
interface IEncoderBuilder extends ICoderBuilder {

	/**
	 * Length of one break between signal's pulses. Optional parameter for an
	 * encoder, when not provided it will be calculated as 2.0*tOnePulse
	 * 
	 * @param tBreak Length of one break between signal's pulses in seconds
	 * @return {@link IEncoderBuilder}
	 */
	IEncoderBuilder tBreak(double tBreak);

	/**
	 * Length of one pulse fade in/out given as % of one pulse length (tOnePulse).
	 * Optional parameter for an encoder, when not provided it will be set as 5%
	 * 
	 * @param fadeLength Length of one pulse fade in/out given as % of one pulse length (tOnePulse)
	 * @return {@link IEncoderBuilder}
	 */
	IEncoderBuilder fadeLength(double fadeLength);

}