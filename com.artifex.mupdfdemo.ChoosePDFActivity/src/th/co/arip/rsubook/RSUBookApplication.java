package th.co.arip.rsubook;

import org.acra.ACRA;
import org.acra.ReportField;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;

import android.app.Application;

@ReportsCrashes(formKey = "", customReportContent = {
		ReportField.APP_VERSION_CODE, ReportField.APP_VERSION_NAME,
		ReportField.ANDROID_VERSION, ReportField.PHONE_MODEL,
		ReportField.CUSTOM_DATA, ReportField.STACK_TRACE, ReportField.LOGCAT },
		mode = ReportingInteractionMode.TOAST, resToastText = R.string.error)
public class RSUBookApplication extends Application {

	@Override
	public void onCreate() {
		super.onCreate();
		
		ACRA.init(this);
	}

}
