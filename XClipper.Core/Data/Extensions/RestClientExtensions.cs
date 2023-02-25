using RestSharp;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Runtime.CompilerServices;
using System.Text;
using System.Threading.Tasks;

namespace Components
{
    public static class RestClientExtensions
    {
        public static RestRequest AddHeaders(this RestRequest request, Dictionary<string, string> headers)
        {
            foreach (var header in headers)
            {
                request.AddHeader(header.Key, header.Value);
            }
            return request;
        }
    }
}
