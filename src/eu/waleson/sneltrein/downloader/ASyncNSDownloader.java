package eu.waleson.sneltrein.downloader;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URL;
import java.net.URLConnection;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.codec.binary.Base64;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import eu.waleson.sneltrein.R;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.os.AsyncTask;

public abstract class ASyncNSDownloader extends
		AsyncTask<ContentHandler, Integer, Integer> {

	String[] args;
	public volatile boolean failed = false;
	public ProgressDialog dialog;
	String url;
	public ContentHandler handler;
	protected boolean sendEmail = false;
	protected String emailText = "";

	public ASyncNSDownloader(String url, ContentHandler handler,
			ProgressDialog dialog) {
		this.url = url;
		this.dialog = dialog;
		this.handler = handler;
		if (dialog != null) {
			dialog.setOnCancelListener(new OnCancelListener() {
				@Override
				public void onCancel(DialogInterface dialog) {
					cancel(true);
				}
			});
		}
	}

	@Override
	protected Integer doInBackground(ContentHandler... handlers) {

		publishProgress(0);
		SAXParserFactory factory = SAXParserFactory.newInstance();
		SAXParser parser;
		try {
			factory.setNamespaceAware(true);
			parser = factory.newSAXParser();
			XMLReader xr = parser.getXMLReader();
			URL lurl = new URL(url);
			URLConnection uc = lurl.openConnection();
			uc.setConnectTimeout(60000);
			uc.setReadTimeout(60000);
			String userpass = "EMAIL:API_KEY";
			if ("EMAIL:API_KEY".equals(userpass)) {
				throw new Exception("Hey man, you need to fill in your key here!");
			}
			
			String basicAuth = "Basic "
					+ new String(new Base64().encode(userpass.getBytes()));
			uc.setRequestProperty("User-agent", "SnelTrein 3.0");
			uc.setRequestProperty("Authorization", basicAuth);
			if (!failed) {
				publishProgress(0);
				xr.setContentHandler(handler);
			}
			if (!failed) {
				xr.parse(new InputSource(uc.getInputStream()));
			}
		} catch (Exception e) {
			e.printStackTrace();
			final Writer result = new StringWriter();
			final PrintWriter printWriter = new PrintWriter(result);
			e.printStackTrace(printWriter);
			emailText = result.toString();
			sendEmail = true;
		}
		return 0;
	}

	@Override
	protected void onCancelled() {
		failed = true;
	}

	@Override
	protected void onProgressUpdate(Integer... i) {
		int resid = -1;
		switch (i[0]) {
		case 0:
			resid = R.string.communicating_site;
			break;
		case 1:
			resid = R.string.parsing_content;
			break;
		}
		if (resid > -1) {
			if (dialog != null)
				dialog.setMessage(dialog.getContext().getString(resid));
		}
	}

	protected void ShowMessageAndQuit() {
		if (dialog != null) {
			AlertDialog.Builder alertbox = new AlertDialog.Builder(
					dialog.getOwnerActivity());
			alertbox.setMessage(R.string.no_information_currently);
			alertbox.setNeutralButton("Ok",
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface arg0, int arg1) {
							dialog.getOwnerActivity().finish();
						}
					});
			alertbox.show();
		}
	}

}
