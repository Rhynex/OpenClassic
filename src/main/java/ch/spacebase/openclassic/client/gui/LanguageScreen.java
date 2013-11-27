package ch.spacebase.openclassic.client.gui;

import java.util.ArrayList;
import java.util.List;

import ch.spacebase.openclassic.api.OpenClassic;
import ch.spacebase.openclassic.api.gui.GuiComponent;
import ch.spacebase.openclassic.api.gui.base.Button;
import ch.spacebase.openclassic.api.gui.base.ButtonCallback;
import ch.spacebase.openclassic.api.gui.base.ButtonList;
import ch.spacebase.openclassic.api.gui.base.ButtonListCallback;
import ch.spacebase.openclassic.api.gui.base.DefaultBackground;
import ch.spacebase.openclassic.api.gui.base.Label;
import ch.spacebase.openclassic.api.translate.Language;

public class LanguageScreen extends GuiComponent {

	private GuiComponent parent;

	public LanguageScreen(GuiComponent parent) {
		super("languagescreen");
		this.parent = parent;
	}

	@Override
	public void onAttached(GuiComponent oparent) {
		this.setSize(parent.getWidth(), parent.getHeight());
		this.attachComponent(new DefaultBackground("bg"));
		ButtonList list = new ButtonList("languages", 0, 0, this.getWidth(), this.getHeight());
		list.setCallback(new ButtonListCallback() {
			@Override
			public void onButtonListClick(ButtonList list, Button button) {
				String code = button.getText().substring(button.getText().indexOf('(') + 1, button.getText().indexOf(')'));
				OpenClassic.getGame().getConfig().setValue("options.language", code);
				OpenClassic.getClient().setActiveComponent(new LanguageScreen(parent));
				OpenClassic.getGame().getConfig().save();
			}
		});
		
		this.attachComponent(list);
		this.attachComponent(new Button("back", this.getWidth() / 2 - 150, this.getHeight() / 6 + 312, 300, 40, OpenClassic.getGame().getTranslator().translate("gui.back")).setCallback(new ButtonCallback() {
			@Override
			public void onButtonClick(Button button) {
				OpenClassic.getClient().setActiveComponent(parent);
			}
		}));
		
		this.attachComponent(new Label("title", this.getWidth() / 2, 30, OpenClassic.getGame().getTranslator().translate("gui.language.select"), true));
		
		String text = String.format(OpenClassic.getGame().getTranslator().translate("gui.language.current"), OpenClassic.getGame().getConfig().getString("options.language"));
		this.attachComponent(new Label("current", this.getWidth() / 2, this.getHeight() / 2 + 96, text, true));
		
		List<String> languages = new ArrayList<String>();
		for (Language language : OpenClassic.getGame().getTranslator().getLanguages()) {
			languages.add(language.getName() + " (" + language.getLangCode() + ")");
		}

		this.getComponent("languages", ButtonList.class).setContents(languages);
	}
	
	@Override
	public void update(int mouseX, int mouseY) {
		String text = String.format(OpenClassic.getGame().getTranslator().translate("gui.language.current"), OpenClassic.getGame().getConfig().getString("options.language"));
		Label label = this.getComponent("current", Label.class);
		if(!label.getText().equals(text)) {
			label.setText(text);
		}
		
		super.update(mouseX, mouseY);
	}
	
}
