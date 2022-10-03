package ultrasound;

import ultrasound.ICoder.CoderMode;

/**
 * Interface for AbstractCoder's Builder
 *
 */
public interface ICoderBuilder {

	/**
	 * Length of one signal's pulse. Optional parameter for a coder, when not
	 * provided it will be calculated as 2.0/freqStep
	 */
	ICoderBuilder tOnePulse(double tOnePulse);

	/**
	 * Coder working mode. Optional parameter, when not provided
	 * {@link CoderMode#DATA_FRAME} will be set.
	 */
	ICoderBuilder mode(CoderMode mode);

	/**
	 * Optional parameter to enable/disable SECDED transmission encoding. It is
	 * enable by default
	 */
	ICoderBuilder secdedEnabled(boolean secdedEnabled);

	/**
	 * This method should return a new {@link ICoder} object when all parameters
	 * were correctly validated.
	 */
	ICoder build();

}
