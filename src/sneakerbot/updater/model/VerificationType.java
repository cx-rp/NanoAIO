package sneakerbot.updater.model;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;

@XmlEnum
@XmlType
public enum VerificationType {
	checksum,
	hash,
	signature
}