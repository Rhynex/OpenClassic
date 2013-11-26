package ch.spacebase.openclassic.client.gui;

import java.util.ArrayList;
import java.util.List;

import ch.spacebase.openclassic.api.OpenClassic;
import ch.spacebase.openclassic.api.gui.GuiScreen;
import ch.spacebase.openclassic.api.gui.widget.Button;
import ch.spacebase.openclassic.api.gui.widget.ButtonList;
import ch.spacebase.openclassic.api.gui.widget.ButtonListCallback;
import ch.spacebase.openclassic.api.gui.widget.Label;
import ch.spacebase.openclassic.api.gui.widget.WidgetFactory;
import ch.spacebase.openclassic.api.translate.Language;

public class LanguageScreen extends GuiScreen {

	private GuiScreen parent;

	public LanguageScreen(GuiScreen parent) {
		this.parent = parent;
	}

	public void onOpen() {
		this.clearWidgets();
		this.attachWidget(WidgetFactory.getFactory().newDefaultBackground(0, this));
		ButtonList list = new ButtonList(1, this);
		list.setCallback(new ButtonListCallback() {
			@Override
			public void onButtonListClick(ButtonList list, Button button) {
				String code = button.getText().substring(button.getText().indexOf('(') + 1, button.getText().indexOf(')'));
				OpenClassic.getGame().getConfig().setValue("options.language", code);
				OpenClassic.getClient().setCurrentScreen(new LanguageScreen(parent));
				OpenClassic.getGame().getConfig().save();
			}
		});
		
		this.attachWidget(list);
		this.attachWidget(WidgetFactory.getFactory().newButton(2, this.getWidth() / 2 - 75, this.getHeight() / 6 + 156, 150, 20, this, OpenClassic.getGame().getTranslator().translate("gui.back")));
		this.attachWidget(WidgetFactory.getFactory().newLabel(3, this.getWidth() / 2, 15, this, OpenClassic.getGame().getTranslator().translate("gui.language.select"), true));
		
		String text = String.format(OpenClassic.getGame().getTranslator().translate("gui.language.current"), OpenClassic.getGame().getConfig().getString("options.language"));
		this.attachWidget(WidgetFactory.getFactory().newLabel(4, this.getWidth() / 2, this.getHeight() / 2 + 48, this, text, true));
		
		List<String> languages = new ArrayList<String>();
		for (Language language : OpenClassic.getGame().getTranslator().getLanguages()) {
			languages.add(language.getName() + " (" + language.getLangCode() + ")");
		}

		this.getWidget(1, ButtonList.class).setContents(languages);
	}

	public void onButtonClick(Button button) {
		if(button.getId() == 2) {
			OpenClassic.getClient().setCurrentScreen(this.parent);
		}
	}
	
	@Override
	public void update(int mouseX, int mouseY) {
		String text = String.format(OpenClassic.getGame().getTranslator().translate("gui.language.current"), OpenClassic.getGame().getConfig().getString("options.language"));
		Label label = this.getWidget(4, Label.class);
		if(!label.getText().equals(text)) {
			label.setText(text);
		}
	}
	
}
