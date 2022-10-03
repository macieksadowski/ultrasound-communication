package ultrasound.decoder;

import ultrasound.dataframe.CheckAddressResult;
import ultrasound.dataframe.ParserResult;

public interface IDecoder extends IDecoderSimple {

	boolean endOfTransmissionReceived();

	void setDeviceAddress(Byte adr);

	ParserResult getParserResult();

	CheckAddressResult getCheckAddressParserResult();

}