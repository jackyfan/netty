package com.jackyfan.netty.pio;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.util.CharsetUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.regex.Pattern;

import javax.activation.MimetypesFileTypeMap;

public class HttpFileServerHandler extends SimpleChannelInboundHandler<FullHttpRequest> {
	private final String url;
	private static final Pattern INSERCURE_URI = Pattern.compile(".*[<>&\"].*");

	public HttpFileServerHandler(String url) {
		this.url = url;

	}

	@Override
	protected void messageReceived(ChannelHandlerContext ctx, FullHttpRequest request) throws Exception {
		if (!request.method().equals(HttpMethod.GET)) {
			sendError(ctx, HttpResponseStatus.METHOD_NOT_ALLOWED);
			return;
		}
		final String uri = request.uri();
		final String path = this.sanitizeUri(uri);
		if (path == null) {
			sendError(ctx, HttpResponseStatus.FORBIDDEN);
			return;
		}
		File file = new File(path);
		if (file.isHidden() || !file.exists()) {
			sendError(ctx, HttpResponseStatus.NOT_FOUND);
			return;
		}
		if (file.isDirectory()) {
			if (uri.endsWith("/")) {
				sendListing(ctx, file);
			} else {
				sendRedirect(ctx, uri + '/');
			}
			return;
		}
		if (!file.isFile()) {
			sendError(ctx, HttpResponseStatus.FORBIDDEN);
			return;
		}
		RandomAccessFile rFile = null;
		try {
			rFile=new RandomAccessFile(file,"r");
		} catch (FileNotFoundException e) {
			sendError(ctx,HttpResponseStatus.NOT_FOUND);
		}
	}

	private String sanitizeUri(String uri) {
		try {
			uri = URLDecoder.decode(uri, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			try {
				uri = URLDecoder.decode(uri, "ISO-8859-1");
			} catch (UnsupportedEncodingException e1) {
				throw new Error();
			}
		}
		if (!uri.startsWith(url)) {
			return null;
		}
		if (!uri.startsWith("/")) {
			return null;
		}
		uri = uri.replace('/', File.separatorChar);
		if (uri.contains(File.separator + '.') || uri.contains('.' + File.separator)
				|| uri.contains(File.separator + '.') || INSERCURE_URI.matcher(uri).matches()) {
			return null;
		}
		return System.getProperty("user.dir") + File.separator + uri;
	}

	private static final void sendListing(ChannelHandlerContext ctx, File dir) {
		FullHttpResponse resp = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
		resp.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/html;charset=UTF-8");
		StringBuilder sb = new StringBuilder();
		String direPath = dir.getPath();
		sb.append("<!DOCTYPE html>\r\n");
		sb.append("<html><head><title>");
		sb.append(direPath);
		sb.append("目录：");
		sb.append("</head></title><body>\r\n");
		sb.append("<h3>");
		sb.append(direPath);
		sb.append("目录：");
		sb.append("</h3>\r\n<ul>");
		sb.append("<li>链接：<a href=\"../\">..</a></li>\r\n");
		String name = "";
		for (File f : dir.listFiles()) {
			if (f.isHidden() || !f.exists()) {
				continue;
			}
			name = f.getName();
			sb.append("<li>链接：<a href=\"");
			sb.append(name);
			sb.append("\">");
			sb.append(name);
			sb.append("</a></li>\r\n");
		}
		sb.append("</ul>");
		ByteBuf bf = Unpooled.copiedBuffer(sb, CharsetUtil.UTF_8);
		resp.content().writeBytes(bf);
		bf.release();
		ctx.writeAndFlush(resp).addListener(ChannelFutureListener.CLOSE);

	}

	private static void sendRedirect(ChannelHandlerContext ctx, String newUrl) {
		FullHttpResponse resp = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.FOUND);
		resp.headers().set(HttpHeaderNames.LOCATION, newUrl);
		ctx.writeAndFlush(resp).addListener(ChannelFutureListener.CLOSE);

	}

	private static void sendError(ChannelHandlerContext ctx, HttpResponseStatus status) {
		FullHttpResponse resp = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status, Unpooled.copiedBuffer(
				"Failure:" + status.toString() + "\r\n", CharsetUtil.UTF_8));
		resp.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain; charset=UFT-8");
		ctx.writeAndFlush(resp).addListener(ChannelFutureListener.CLOSE);
	}

	private static void setContentTypeHeader(HttpResponse response, File file) {
		MimetypesFileTypeMap map = new MimetypesFileTypeMap();
		response.headers().set(HttpHeaderNames.CONTENT_TYPE, map.getContentType(file));
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		// TODO Auto-generated method stub
		super.exceptionCaught(ctx, cause);
	}

}
