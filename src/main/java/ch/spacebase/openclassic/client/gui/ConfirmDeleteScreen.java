package ch.spacebase.openclassic.client.gui;

import java.io.File;

import ch.spacebase.openclassic.api.OpenClassic;
import ch.spacebase.openclassic.api.gui.GuiScreen;
import ch.spacebase.openclassic.api.gui.widget.Button;
import ch.spacebase.openclassic.api.render.RenderHelper;
import ch.spacebase.openclassic.client.util.GeneralUtils;

/**
 * @author Steveice10 <Steveice10@gmail.com>
 */
public class ConfirmDeleteScreen extends GuiScreen {

	private GuiScreen parent;
	private String name;
	private File file;

	public ConfirmDeleteScreen(GuiScreen parent, String name, File file) {
		this.parent = parent;
		this.name = name;
		this.file = file;
	}

	public void onOpen() {
		this.clearWidgets();
		this.attachWidget(new Button(0, this.getWidth() / 2 - 102, this.getHeight() / 6 + 132, 100, 20, this, OpenClassic.getGame().getTranslator().translate("gui.yes")));
		this.attachWidget(new Button(0, this.getWidth() / 2 + 2, this.getHeight() / 6 + 132, 100, 20, this, OpenClassic.getGame().getTranslator().translate("gui.no")));
	}

	public void onButtonClick(Button button) {
		if(button.getId() == 0) {
			try {
				this.file.delete();
			} catch(SecurityException e) {
				e.printStackTrace();
			}

			File file = new File(new File(GeneralUtils.getMinecraft().dir, "levels"), this.name + ".nbt");
			if(file.exists()) {
				try {
					file.delete();
				} catch(SecurityException e) {
					e.printStackTrace();
				}
			}
		}

		OpenClassic.getClient().setCurrentScreen(this.parent);
	}

	public void render() {
		RenderHelper.getHelper().drawDefaultBG();

		RenderHelper.getHelper().renderText(String.format(OpenClassic.getGame().getTranslator().translate("gui.delete.level"), this.file.getName().substring(0, this.file.getName().indexOf("."))), this.getWidth() / 2, (this.getHeight() / 2) - 32);
		super.render();
	}
}
