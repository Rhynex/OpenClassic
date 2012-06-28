package com.mojang.minecraft.model;

import com.mojang.minecraft.model.Model;
import com.mojang.minecraft.model.ModelRenderer;
import com.mojang.util.MathHelper;

public final class SpiderModel extends Model {

	private ModelRenderer b = new ModelRenderer(32, 4);
	private ModelRenderer c;
	private ModelRenderer d;
	private ModelRenderer e;
	private ModelRenderer f;
	private ModelRenderer g;
	private ModelRenderer h;
	private ModelRenderer i;
	private ModelRenderer j;
	private ModelRenderer k;
	private ModelRenderer l;

	public SpiderModel() {
		this.b.addBox(-4.0F, -4.0F, -8.0F, 8, 8, 8, 0.0F);
		this.b.setPos(0.0F, 0.0F, -3.0F);
		this.c = new ModelRenderer(0, 0);
		this.c.addBox(-3.0F, -3.0F, -3.0F, 6, 6, 6, 0.0F);
		this.d = new ModelRenderer(0, 12);
		this.d.addBox(-5.0F, -4.0F, -6.0F, 10, 8, 12, 0.0F);
		this.d.setPos(0.0F, 0.0F, 9.0F);
		this.e = new ModelRenderer(18, 0);
		this.e.addBox(-15.0F, -1.0F, -1.0F, 16, 2, 2, 0.0F);
		this.e.setPos(-4.0F, 0.0F, 2.0F);
		this.f = new ModelRenderer(18, 0);
		this.f.addBox(-1.0F, -1.0F, -1.0F, 16, 2, 2, 0.0F);
		this.f.setPos(4.0F, 0.0F, 2.0F);
		this.g = new ModelRenderer(18, 0);
		this.g.addBox(-15.0F, -1.0F, -1.0F, 16, 2, 2, 0.0F);
		this.g.setPos(-4.0F, 0.0F, 1.0F);
		this.h = new ModelRenderer(18, 0);
		this.h.addBox(-1.0F, -1.0F, -1.0F, 16, 2, 2, 0.0F);
		this.h.setPos(4.0F, 0.0F, 1.0F);
		this.i = new ModelRenderer(18, 0);
		this.i.addBox(-15.0F, -1.0F, -1.0F, 16, 2, 2, 0.0F);
		this.i.setPos(-4.0F, 0.0F, 0.0F);
		this.j = new ModelRenderer(18, 0);
		this.j.addBox(-1.0F, -1.0F, -1.0F, 16, 2, 2, 0.0F);
		this.j.setPos(4.0F, 0.0F, 0.0F);
		this.k = new ModelRenderer(18, 0);
		this.k.addBox(-15.0F, -1.0F, -1.0F, 16, 2, 2, 0.0F);
		this.k.setPos(-4.0F, 0.0F, -1.0F);
		this.l = new ModelRenderer(18, 0);
		this.l.addBox(-1.0F, -1.0F, -1.0F, 16, 2, 2, 0.0F);
		this.l.setPos(4.0F, 0.0F, -1.0F);
	}

	public final void a(float var1, float var2, float var3, float var4, float var5, float var6) {
		this.b.g = var4 / 57.295776F;
		this.b.f = var5 / 57.295776F;
		var4 = 0.7853982F;
		this.e.h = -var4;
		this.f.h = var4;
		this.g.h = -var4 * 0.74F;
		this.h.h = var4 * 0.74F;
		this.i.h = -var4 * 0.74F;
		this.j.h = var4 * 0.74F;
		this.k.h = -var4;
		this.l.h = var4;
		var4 = 0.3926991F;
		this.e.g = var4 * 2.0F;
		this.f.g = -var4 * 2.0F;
		this.g.g = var4;
		this.h.g = -var4;
		this.i.g = -var4;
		this.j.g = var4;
		this.k.g = -var4 * 2.0F;
		this.l.g = var4 * 2.0F;
		var4 = -(MathHelper.cos(var1 * 0.6662F * 2.0F) * 0.4F) * var2;
		var5 = -(MathHelper.cos(var1 * 0.6662F * 2.0F + 3.1415927F) * 0.4F) * var2;
		float var7 = -(MathHelper.cos(var1 * 0.6662F * 2.0F + 1.5707964F) * 0.4F) * var2;
		float var8 = -(MathHelper.cos(var1 * 0.6662F * 2.0F + 4.712389F) * 0.4F) * var2;
		float var9 = Math.abs(MathHelper.sin(var1 * 0.6662F) * 0.4F) * var2;
		float var10 = Math.abs(MathHelper.sin(var1 * 0.6662F + 3.1415927F) * 0.4F) * var2;
		float var11 = Math.abs(MathHelper.sin(var1 * 0.6662F + 1.5707964F) * 0.4F) * var2;
		var2 = Math.abs(MathHelper.sin(var1 * 0.6662F + 4.712389F) * 0.4F) * var2;
		this.e.g += var4;
		this.f.g -= var4;
		this.g.g += var5;
		this.h.g -= var5;
		this.i.g += var7;
		this.j.g -= var7;
		this.k.g += var8;
		this.l.g -= var8;
		this.e.h += var9;
		this.f.h -= var9;
		this.g.h += var10;
		this.h.h -= var10;
		this.i.h += var11;
		this.j.h -= var11;
		this.k.h += var2;
		this.l.h -= var2;
		this.b.a(var6);
		this.c.a(var6);
		this.d.a(var6);
		this.e.a(var6);
		this.f.a(var6);
		this.g.a(var6);
		this.h.a(var6);
		this.i.a(var6);
		this.j.a(var6);
		this.k.a(var6);
		this.l.a(var6);
	}
}
