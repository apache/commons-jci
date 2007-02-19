package org.apache.commons.jci.classes;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;


public class ExtendedDump implements Opcodes {

	public static byte[] dump() throws Exception {

		ClassWriter cw = new ClassWriter(true);
//		FieldVisitor fv;
		MethodVisitor mv;
//		AnnotationVisitor av0;

		cw.visit(V1_4, ACC_PUBLIC + ACC_SUPER, "jci/Extended", null, "jci/Simple", null);

		cw.visitSource("Extended.java", null);

		{
			mv = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
			mv.visitCode();
			Label l0 = new Label();
			mv.visitLabel(l0);
			mv.visitLineNumber(3, l0);
			mv.visitVarInsn(ALOAD, 0);
			mv.visitMethodInsn(INVOKESPECIAL, "jci/Simple", "<init>", "()V");
			mv.visitInsn(RETURN);
			Label l1 = new Label();
			mv.visitLabel(l1);
			mv.visitLocalVariable("this", "Ljci/Extended;", null, l0, l1, 0);
			mv.visitMaxs(1, 1);
			mv.visitEnd();
		}
		{
			mv = cw.visitMethod(ACC_PUBLIC, "toString", "()Ljava/lang/String;", null, null);
			mv.visitCode();
			Label l0 = new Label();
			mv.visitLabel(l0);
			mv.visitLineNumber(6, l0);
			mv.visitTypeInsn(NEW, "java/lang/StringBuffer");
			mv.visitInsn(DUP);
			mv.visitLdcInsn("Extended:");
			mv.visitMethodInsn(INVOKESPECIAL, "java/lang/StringBuffer", "<init>", "(Ljava/lang/String;)V");
			mv.visitVarInsn(ALOAD, 0);
			mv.visitMethodInsn(INVOKESPECIAL, "jci/Simple", "toString", "()Ljava/lang/String;");
			mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuffer", "append", "(Ljava/lang/String;)Ljava/lang/StringBuffer;");
			mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuffer", "toString", "()Ljava/lang/String;");
			mv.visitInsn(ARETURN);
			Label l1 = new Label();
			mv.visitLabel(l1);
			mv.visitLocalVariable("this", "Ljci/Extended;", null, l0, l1, 0);
			mv.visitMaxs(3, 1);
			mv.visitEnd();
		}
		cw.visitEnd();

		return cw.toByteArray();
	}
}
