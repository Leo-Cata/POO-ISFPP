package md.ui.impl.javafx;

import javafx.application.Application;
import md.ui.api.UIEngine;

public class JavaFXUserInterface implements UIEngine {

	@Override
	public void start() {
		Application.launch(MainApp.class);
	}

}
