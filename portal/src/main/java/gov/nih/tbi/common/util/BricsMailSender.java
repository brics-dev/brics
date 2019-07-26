package gov.nih.tbi.common.util;

import java.io.Serializable;

import org.springframework.mail.javamail.JavaMailSenderImpl;

public class BricsMailSender extends JavaMailSenderImpl implements Serializable{
	private static final long serialVersionUID = -3682599395739028450L;
}
