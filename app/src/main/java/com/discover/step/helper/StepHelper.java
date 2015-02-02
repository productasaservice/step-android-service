package com.discover.step.helper;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;

import com.discover.step.StepApplication;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

/**
 * Created by Geri on 2015.01.24..
 */
public class StepHelper {

    public static void getKeyHash() {
        try {
            Context context = StepApplication.getContext();
            PackageInfo info = context.getPackageManager().getPackageInfo(
                    context.getPackageName(),
                    PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.d("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        } catch (PackageManager.NameNotFoundException e) {
            Log.d("Error1", "NameNotFoundException");

        } catch (NoSuchAlgorithmException e) {
            Log.d("Error2", "Algorthim");
        }
    }

    public static void getSHA1Key() {
        PackageInfo packageInfo = null;
        try {
            Context context = StepApplication.getContext();
            packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), PackageManager.GET_SIGNATURES);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        Signature[] signatures = packageInfo.signatures;
        byte[] cert = signatures[0].toByteArray();
        InputStream input = new ByteArrayInputStream(cert);

        CertificateFactory cf = null;
        try {
            cf = CertificateFactory.getInstance("X509");
        } catch (CertificateException e) {
            e.printStackTrace();
        }
        X509Certificate c = null;
        try {
            c = (X509Certificate) cf.generateCertificate(input);
        } catch (CertificateException e) {
            e.printStackTrace();
        }
        try {
            MessageDigest md = MessageDigest.getInstance("SHA1");
            byte[] publicKey = md.digest(c.getPublicKey().getEncoded());

            StringBuffer hexString = new StringBuffer();
            for (int i=0;i<publicKey.length;i++) {
                String appendString = Integer.toHexString(0xFF & publicKey[i]);
                if(appendString.length()==1)hexString.append("0");
                hexString.append(appendString);
            }
            Log.d("SHA1", "Cer: " + hexString.toString());

        } catch (NoSuchAlgorithmException e1) {
            e1.printStackTrace();
        }
    }

    public static void setListViewHeightBasedOnChildren(BaseAdapter baseAdapter, ListView listView) {
        int totalHeight = 0;
        for (int i = 0; i < baseAdapter.getCount(); i++) {
            View listItem = baseAdapter.getView(i, null, listView);
            listItem.measure(0, 0);
            totalHeight += listItem.getMeasuredHeight();
        }
        totalHeight += 40;
        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (baseAdapter.getCount() - 1));
        listView.setLayoutParams(params);
    }

}
