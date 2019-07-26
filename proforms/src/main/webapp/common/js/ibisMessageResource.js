
function IbisMessageResource()
{
	var language = window.navigator.userLanguage || window.navigator.language;
	
	// --------------------------------------------------------------
	// Language Definitions
	
	/**
	 * Messages in U.S. English
	 */
	var oMessages_en_US = {
		getDownloadConfirmMsg : function(fileName)
		{
			return "Would you like to download the \"" + fileName + "\" file?";
		},
		getFileNoDataErrMsg : function(fileName)
		{
			return "The \"" + fileName + "\" file node is not associated with any file data.";
		},
		getFolderDeleteSuccessMsg : function(folderName)
		{
			return "The \"" + folderName + "\" folder has been deleted successfully.";
		}
	};
	
	/**
	 * Messages in Chinese (Simplified)
	 */
	var oMessages_zh_TW = {
		getDownloadConfirmMsg : function(fileName)
		{
			return "你想下载的\"" + fileName + "\"的文件?";
		},
		getFileNoDataErrMsg : function(fileName)
		{
			return "是不相关的任何文件的数据\"" + fileName + "\"的文件节点";
		},
		getFolderDeleteSuccessMsg : function(folderName)
		{
			return "\"" + folderName + "\"的文件夹中已成功删除";
		}
	};
	
	/**
	 * Messages in Chinese (Traditional)
	 */
	var oMessgaes_zh_CN = {
		getDownloadConfirmMsg : function(fileName)
		{
			return "你想下載的\"" + fileName + "\"的文件？";
		},
		getFileNoDataErrMsg : function(fileName)
		{
			return "是不相關的任何文件的數據\"" + fileName + "\"的文件節點";
		},
		getFolderDeleteSuccessMsg : function(folderName)
		{
			return "\"" + folderName + "\"的文件夾中已成功刪除";
		}
	};
	
	/**
	 * Messages in Korean
	 */
	var oMessgaes_ko_KR = {
		getDownloadConfirmMsg : function(fileName)
		{
			return "당신은 \"" + fileName + "\" 파일을 다운로드 하시겠습니까?";
		},
		getFileNoDataErrMsg : function(fileName)
		{
			return "\"" + fileName + "\" 파일 노드는 모든 파일 데이터와 연결되어 있지 않습니다";
		},
		getFolderDeleteSuccessMsg : function(folderName)
		{
			return "\"" + folderName + "\" 폴더가 삭제되었습니다";
		}
	};
	
	/**
	 * Messages in Spanish
	 */
	var oMessgaes_es = {
		getDownloadConfirmMsg : function(fileName)
		{
			return "¿Quieres descargar el archivo \"" + fileName + "\"?";
		},
		getFileNoDataErrMsg : function(fileName)
		{
			return "El nodo de archivo \"" + fileName + "\" no está asociado a ningún archivo de datos.";
		},
		getFolderDeleteSuccessMsg : function(folderName)
		{
			return "La carpeta \"" + folderName + "\" se ha eliminado correctamente.";
		}
	};
	
	// Decide which language to use
	if ( (typeof language == "undefined") || (language == "") )
	{
		this.messages = oMessages_en_US;
	}
	else if ( language.indexOf("zh-TW") > -1 )
	{
		this.messages = oMessages_zh_TW;
	}
	else if ( language.indexOf("zh-CN") > -1 )
	{
		this.messages = oMessgaes_zh_CN;
	}
	else if ( language.indexOf("ko") > -1 )
	{
		this.messages = oMessgaes_ko_KR;
	}
	else if ( language.indexOf("es") > -1 )
	{
		this.messages = oMessgaes_es;
	}
	else
	{
		this.messages = oMessages_en_US;
	}
}