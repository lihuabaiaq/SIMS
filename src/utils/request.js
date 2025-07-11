import {globals} from "@/main";

const serverUrl = globals.$config?.serverUrl || 'http://localhost:9090'

const request = axios.create({
    baseURL: serverUrl,
    timeout: 30000
})