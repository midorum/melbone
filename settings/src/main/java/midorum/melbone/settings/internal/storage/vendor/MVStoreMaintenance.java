package midorum.melbone.settings.internal.storage.vendor;

import dma.validation.Validator;
import org.h2.mvstore.MVMap;
import org.h2.mvstore.MVStore;

import java.io.File;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

@SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
public class MVStoreMaintenance {

    private static final Set<String> settingsMapSet = Set.of(
            "application",
            "targetLauncher",
            "targetCountControl",
            "targetBaseApp",
            "uac");
    private static final Set<String> stampsMapSet = Set.of(
            "targetLauncherStamp",
            "targetBaseAppStamp");
    private static final Set<String> accountsMapSet = Set.of(
            "accounts",
            "inUse");
    private static final Set<String> requiredMapSet = new HashSet<>(){{
        addAll(settingsMapSet);
        addAll(stampsMapSet);
        addAll(accountsMapSet);
    }};
    private final String path;
    private final String srcStoreFileName;
    private final String dstStoreFileName;
    private final String srcStorePassword;
    private final String dstStorePassword;

    public MVStoreMaintenance() {
        path = Validator.checkNotNull(System.getProperty("storage.path")).orThrow("You should provide path to storage file");
        srcStoreFileName = Validator.checkNotNull(System.getProperty("src.storage.name")).orThrow("You should provide source storage file name");
        dstStoreFileName = Validator.checkNotNull(System.getProperty("dst.storage.name")).orThrow("You should provide destination storage file name");
        srcStorePassword = Validator.checkNotNull(System.getProperty("src.storage.password")).orThrow("You should provide source storage password");
        dstStorePassword = Validator.checkNotNull(System.getProperty("dst.storage.password")).orDefault(srcStorePassword);
    }

    public static void main(String[] args) {
        new MVStoreMaintenance().openStoragesAndDo((instance) -> {
            instance.printStore(instance.srcStore);
            ///instance.cloneStorage();
            ///instance.cloneMaps(requiredMapSet);
            ///instance.removeMap(instance.dstStore, "registryResource2");
            instance.printStore(instance.dstStore);
        });
    }

    public void openStoragesAndDo(final Consumer<Instance> consumer) {
        try (final MVStore srcStore = openStore(path + File.separator + srcStoreFileName, srcStorePassword.toCharArray());
             final MVStore dstStore = openStore(path + File.separator + dstStoreFileName, dstStorePassword.toCharArray())) {
            consumer.accept(new Instance(srcStore, dstStore));
        }
    }

    private MVStore openStore(final String filename, final char[] password) {
        return new MVStore.Builder()
                .fileName(filename)
                .encryptionKey(password)
                .compress()
                .open();
    }

    private record Instance(MVStore srcStore, MVStore dstStore) {

        public void printStore(final MVStore store) {
            System.out.println("Printing \"" + store.getFileStore().getFileName() + "\" storage content: ");
            final Set<String> mapNames = store.getMapNames();
            System.out.println("maps: " + mapNames);
            mapNames.forEach(mapName -> {
                try {
                    System.out.println("\nmap: " + mapName);
                    System.out.println("values:");
                    MVMap<Object, Object> mvMap = store.openMap(mapName);
                    try {
                        mvMap.forEach((k, v) -> System.out.println(k + " : " + v));
                    } catch (Throwable t) {
                        System.err.println("Unable read map " + mapName + " due to " + t.getMessage());
                    }
                } catch (Throwable t) {
                    System.err.println("Unable open map " + mapName + " due to " + t.getMessage());
                }
            });
            System.out.println("\n-----------------------------");
        }

        public void removeMap(final MVStore store, final String mapName) {
            System.out.println("Removing map \"" + mapName + "\" from \"" + store.getFileStore().getFileName() + "\"");
            System.out.println("maps before: " + store.getMapNames());
            System.out.println("removing map \"" + mapName + "\"");
            store.removeMap(mapName);
            System.out.println("maps after: " + store.getMapNames());
            store.commit();
            System.out.println("done");
            System.out.println("-----------------------------");
        }

        public void cloneMaps(final Collection<String> mapsToClone) {
            System.out.println("Cloning maps from \"" + srcStore.getFileStore().getFileName() + "\" to \"" + dstStore.getFileStore().getFileName() + "\"");
            Set<String> mapNames = srcStore.getMapNames();
            mapNames.forEach(mapName -> {
                System.out.println();
                if (!mapsToClone.contains(mapName)) {
                    System.out.println("map " + mapName + " skipped");
                    return;
                }
                System.out.println("cloning map " + mapName);
                MVMap<Object, Object> srcMap = srcStore.openMap(mapName);
                MVMap<Object, Object> dstMap = dstStore.openMap(mapName);
                srcMap.forEach((k, v) -> {
                    System.out.println(k + ":" + v);
                    dstMap.put(k, v);
                });
                System.out.println("map " + mapName + " has been cloned");
            });
            dstStore.commit();
            System.out.println("done");
            System.out.println("-----------------------------");
        }

        public void cloneStorage() {
            System.out.println("Cloning \"" + srcStore.getFileStore().getFileName() + "\" to \"" + dstStore.getFileStore().getFileName() + "\"");
            final Set<String> mapNames = srcStore.getMapNames();
            mapNames.forEach(mapName -> {
                System.out.println("cloning map " + mapName);
                final MVMap<Object, Object> srcMap = srcStore.openMap(mapName);
                final MVMap<Object, Object> dstMap = dstStore.openMap(mapName);
                srcMap.forEach((k, v) -> {
                    System.out.println(k + ":" + v);
                    dstMap.put(k, v);
                });
                System.out.println();
            });
            dstStore.commit();
            System.out.println("done");
            System.out.println("-----------------------------");
        }
    }

}
