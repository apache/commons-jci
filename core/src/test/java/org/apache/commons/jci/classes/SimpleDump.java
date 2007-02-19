package org.apache.commons.jci.classes;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class SimpleDump implements Opcodes {

	public static byte[] dump( final String to ) throws Exception {

	ClassWriter cw = new ClassWriter(true);
//	FieldVisitor fv;
	MethodVisitor mv;
//	AnnotationVisitor av0;

	cw.visit(V1_4, ACC_PUBLIC + ACC_SUPER, "jci/Simple", null, "java/lang/Object", null);

	cw.visitSource("Simple.java", null);

	{
	mv = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
	mv.visitCode();
	Label l0 = new Label();
	mv.visitLabel(l0);
	mv.visitLineNumber(3, l0);
	mv.visitVarInsn(ALOAD, 0);
	mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V");
	mv.visitInsn(RETURN);
	Label l1 = new Label();
	mv.visitLabel(l1);
	mv.visitLocalVariable("this", "Ljci/Simple;", null, l0, l1, 0);
	mv.visitMaxs(1, 1);
	mv.visitEnd();
	}
	{
	mv = cw.visitMethod(ACC_PUBLIC, "toString", "()Ljava/lang/String;", null, null);
	mv.visitCode();
	Label l0 = new Label();
	mv.visitLabel(l0);
	mv.visitLineNumber(6, l0);
	mv.visitLdcInsn(to);
	mv.visitInsn(ARETURN);
	Label l1 = new Label();
	mv.visitLabel(l1);
	mv.visitLocalVariable("this", "Ljci/Simple;", null, l0, l1, 0);
	mv.visitMaxs(1, 1);
	mv.visitEnd();
	}
	cw.visitEnd();

	return cw.toByteArray();
	}
}
