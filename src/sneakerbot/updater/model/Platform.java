package sneakerbot.updater.model;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;

@XmlEnum
@XmlType
public enum Platform {
	mac,
	win_x86,
	win_x64,
	independent
}