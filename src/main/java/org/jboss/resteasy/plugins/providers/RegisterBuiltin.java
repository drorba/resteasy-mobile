package org.jboss.resteasy.plugins.providers;

import org.jboss.resteasy.logging.Logger;
import org.jboss.resteasy.spi.ResteasyProviderFactory;

import javax.ws.rs.ext.Providers;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class RegisterBuiltin
{

   private final static Logger logger = Logger.getLogger(RegisterBuiltin.class);

   private static Set<String> DEFAULT_PROVIDER_SET = new LinkedHashSet<String>();
   static {
       DEFAULT_PROVIDER_SET.add("org.jboss.resteasy.plugins.providers.DataSourceProvider");
       DEFAULT_PROVIDER_SET.add("org.jboss.resteasy.plugins.providers.DocumentProvider");
       DEFAULT_PROVIDER_SET.add("org.jboss.resteasy.plugins.providers.DefaultTextPlain");
       DEFAULT_PROVIDER_SET.add("org.jboss.resteasy.plugins.providers.StringTextStar");
       DEFAULT_PROVIDER_SET.add("org.jboss.resteasy.plugins.providers.InputStreamProvider");
       DEFAULT_PROVIDER_SET.add("org.jboss.resteasy.plugins.providers.ByteArrayProvider");
       DEFAULT_PROVIDER_SET.add("org.jboss.resteasy.plugins.providers.FormUrlEncodedProvider");
       DEFAULT_PROVIDER_SET.add("org.jboss.resteasy.plugins.providers.FileProvider");
       DEFAULT_PROVIDER_SET.add("org.jboss.resteasy.plugins.providers.StreamingOutputProvider");
       DEFAULT_PROVIDER_SET.add("org.jboss.resteasy.plugins.providers.IIOImageProvider");
       DEFAULT_PROVIDER_SET.add("org.jboss.resteasy.plugins.providers.jackson.ResteasyJacksonProvider");
       DEFAULT_PROVIDER_SET.add("org.jboss.resteasy.plugins.interceptors.CacheControlInterceptor");
       DEFAULT_PROVIDER_SET.add("org.jboss.resteasy.plugins.interceptors.encoding.AcceptEncodingGZIPInterceptor");
       DEFAULT_PROVIDER_SET.add("org.jboss.resteasy.plugins.interceptors.encoding.ClientContentEncodingHeaderInterceptor");
       DEFAULT_PROVIDER_SET.add("org.jboss.resteasy.plugins.interceptors.encoding.GZIPDecodingInterceptor");
       DEFAULT_PROVIDER_SET.add("org.jboss.resteasy.plugins.interceptors.encoding.GZIPEncodingInterceptor");
       DEFAULT_PROVIDER_SET.add("org.jboss.resteasy.plugins.interceptors.encoding.ServerContentEncodingHeaderInterceptor");
   }

   public static void register(ResteasyProviderFactory factory)
   {
      synchronized (factory)
      {
         if (factory.isBuiltinsRegistered() || !factory.isRegisterBuiltins()) return;
         try
         {
            registerProviders(factory);
         }
         catch (Exception e)
         {
            throw new RuntimeException(e);
         }
         factory.setBuiltinsRegistered(true);
      }
   }

   public static void registerProviders(ResteasyProviderFactory factory) throws Exception
   {
      Enumeration<URL> en = Thread.currentThread().getContextClassLoader().getResources("META-INF/services/" + Providers.class.getName());
      LinkedHashSet<String> set = new LinkedHashSet<String>();
      while (en.hasMoreElements())
      {
         URL url = en.nextElement();
         InputStream is = url.openStream();
         try
         {
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            String line;
            while ((line = reader.readLine()) != null)
            {
               line = line.trim();
               if (line.equals("")) continue;
               set.add(line);
            }
         }
         finally
         {
            is.close();
         }
      }
      // [TDI] Cannot read Providers from META-INF/services
      // ServiceLoader is only available from android API 9 
      if (set.isEmpty()) {
          set.addAll(DEFAULT_PROVIDER_SET);
      }
      for (String line : set)
      {
         try
         {
            Class clazz = Thread.currentThread().getContextClassLoader().loadClass(line);
            factory.registerProvider(clazz, true);
         }
         catch (NoClassDefFoundError e)
         {
            logger.warn("NoClassDefFoundError: Unable to load builtin provider: " + line);
         }
         catch (ClassNotFoundException e)
         {
            logger.warn("ClassNotFoundException: Unable to load builtin provider: " + line);
         }
      }
   }

}
