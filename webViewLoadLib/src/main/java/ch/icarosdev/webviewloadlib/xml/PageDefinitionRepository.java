package ch.icarosdev.webviewloadlib.xml;

import android.content.Context;
import ch.icarosdev.webviewloadlib.R;
import ch.icarosdev.webviewloadlib.domain.PageBundle;

/**
 * Created with IntelliJ IDEA.
 * User: Florian
 * Date: 27.10.13
 * Time: 14:41
 * To change this template use File | Settings | File Templates.
 */
public class PageDefinitionRepository {

    private final String urlToLoad;
    private PageDefinitionWorkspace pageDefinitionWorkspaceCommon;
    private PageDefinitionWorkspace pageDefinitionWorkspaceCustom;
    private String applicationName;


    public PageDefinitionRepository(Context context) {
        this.applicationName = context.getResources().getString(R.string.app_name);
        this.pageDefinitionWorkspaceCommon = new PageDefinitionWorkspace(this.applicationName, "PageDefinitions.xml");
        this.pageDefinitionWorkspaceCustom = new PageDefinitionWorkspace(this.applicationName, "CustomPageDefinitions.xml");
        this.urlToLoad = context.getString(R.string.url_to_load);
    }

    public void initializeWorkspaces(IDefinitionDownloadedListener definitionDownloadedListener) {
        this.pageDefinitionWorkspaceCommon.initializeDir();
        AsyncDownloadFile asyncDownloadFile = new AsyncDownloadFile(definitionDownloadedListener);

        //Flozi "https://dl.dropboxusercontent.com/s/q5d84q08dajleuu/PageDefinitions.xml?token_hash=AAEy7eUW49wsJdlPMPjcNrJi6eiFsJdjyFJ4n0W-ziXZMA&dl=1"
        //Basisraush: "https://dl.dropboxusercontent.com/s/9w79rg302fpsaj5/PageDefinitions.xml?token_hash=AAF54zKqCBEYX_TaTtPygV4nxNGMHi3d6cXWkVMLxS90lg&dl=1"

        // Flozi
        //asyncDownloadFile.execute("https://dl.dropboxusercontent.com/s/q5d84q08dajleuu/PageDefinitions.xml?token_hash=AAEy7eUW49wsJdlPMPjcNrJi6eiFsJdjyFJ4n0W-ziXZMA&dl=1", this.pageDefinitionWorkspaceCommon.getPath());

        // Basisrausch
        asyncDownloadFile.execute(this.urlToLoad, this.pageDefinitionWorkspaceCommon.getPath());
    }

    public PageBundle readAllDefinitions() {
        PageBundle coreBundle = this.pageDefinitionWorkspaceCommon.readFile();
        PageBundle customBundle = this.pageDefinitionWorkspaceCustom.readFile();

        if(coreBundle != null) {
            coreBundle.mergeBundle(customBundle);
        }
        return coreBundle;
    }

    public PageBundle readCustomDefinitions() {
        PageBundle customBundle = this.pageDefinitionWorkspaceCustom.readFile();
        return customBundle;
    }

    public void persistCommonDefinitions(PageBundle bundle) {
        this.pageDefinitionWorkspaceCommon.persistFile(bundle);
    }

    public void persistCustomDefinitions(PageBundle bundle) {
        this.pageDefinitionWorkspaceCustom.persistFile(bundle);
    }

    public void deleteCustomDefinitions() {
        this.pageDefinitionWorkspaceCustom.deleteFile();
    }
}
