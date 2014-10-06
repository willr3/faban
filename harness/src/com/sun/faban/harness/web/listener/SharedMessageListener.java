package com.sun.faban.harness.web.listener;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.event.PhaseEvent;
import javax.faces.event.PhaseId;
import javax.faces.event.PhaseListener;

//Enables sharing messages between redirects
public class SharedMessageListener implements PhaseListener{

	/**
	 * 
	 */
	private static final long serialVersionUID = 3088871236588370324L;

	private static final String SESSION_KEY="SHARED_MESSAGE_LIST";
	
	@SuppressWarnings("unchecked")
	private int saveMessages(final FacesContext context){
		List<FacesMessage> messages = new LinkedList<FacesMessage>();
		List<FacesMessage> ctxMessages = context.getMessageList();
		
		if(ctxMessages.isEmpty())
			return 0;
		messages.addAll(ctxMessages);
		Map<String,Object> sessionMap = context.getExternalContext().getSessionMap();
		
		List<FacesMessage> existing = (List<FacesMessage>)sessionMap.get(SESSION_KEY);
		if(existing!=null){
			existing.addAll(messages);
		}else{
			sessionMap.put(SESSION_KEY, messages);
		}
		return messages.size();
	}
	@SuppressWarnings("unchecked")
	private int loadMessages(final FacesContext context){
		Map<String,Object> sessionMap = context.getExternalContext().getSessionMap();
		List<FacesMessage> messages = (List<FacesMessage>)sessionMap.remove(SESSION_KEY);
		if(messages==null)
			return 0;
		
		for(FacesMessage m : messages){
			context.addMessage(null, m);
		}
		
		return messages.size();
	}
	
	@Override
	public void afterPhase(PhaseEvent event) {
		if(PhaseId.RENDER_RESPONSE.equals(event.getPhaseId())){
			saveMessages(event.getFacesContext());
		}
		
	}

	@Override
	public void beforePhase(PhaseEvent event) {
		FacesContext context = event.getFacesContext();
		saveMessages(context);
		if(PhaseId.RENDER_RESPONSE.equals(event.getPhaseId())){
			if(!context.getResponseComplete()){
				loadMessages(context);
			}
		}
	}

	@Override
	public PhaseId getPhaseId() {
		return PhaseId.ANY_PHASE;
	}

	
		
}
